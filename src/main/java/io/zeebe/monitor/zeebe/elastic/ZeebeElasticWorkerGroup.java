package io.zeebe.monitor.zeebe.elastic;

import io.zeebe.monitor.entity.ElasticTimeSeries;
import io.zeebe.monitor.entity.StatusWork;
import io.zeebe.monitor.repository.ElasticTimeSeriesRepository;
import io.zeebe.monitor.zeebe.hazelcast.importers.ImporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "zeebe-importer", havingValue = "elastic")
public class ZeebeElasticWorkerGroup {

    private static final Logger LOG = LoggerFactory.getLogger(ZeebeElasticWorkerGroup.class);
    private static final int COUNT_THREAD = 8;

    @Value("${elastic.index}")
    private String elasticIndex;

    private final ElasticTimeSeriesRepository timeSeriesRepository;
    private final ImporterFactory importerFactory;
    private final ElasticClientProvider clientProvider;
    private final ConcurrentHashMap<Integer, ZeebeElasticImporterWorker> workersInfo = new ConcurrentHashMap<>(COUNT_THREAD);
    private final ExecutorService workers = Executors.newFixedThreadPool(COUNT_THREAD);
    private final BlockingQueue<ElasticTimeSeries> timeSeries = new LinkedBlockingQueue<>();
    private final BlockingQueue<Integer> freeWorkers = new ArrayBlockingQueue<>(COUNT_THREAD);

    private boolean inProcess = true;

    public ZeebeElasticWorkerGroup(ElasticTimeSeriesRepository timeSeriesRepository, ImporterFactory importerFactory, ElasticClientProvider clientProvider) {
        this.timeSeriesRepository = timeSeriesRepository;
        this.importerFactory = importerFactory;
        this.clientProvider = clientProvider;
        init();
    }

    private void init() {
        for (var i = 0; i < COUNT_THREAD; i++) {
            var client = clientProvider.getClient();
            var worker = new ZeebeElasticImporterWorker(i, client, getCallback(), elasticIndex, importerFactory);
            workersInfo.put(i, worker);
        }
    }

    @Scheduled(initialDelay = 1000 * 30, fixedDelay=Long.MAX_VALUE, timeUnit = TimeUnit.NANOSECONDS )
    public void startWork() {
        while (inProcess) {
            try {
                var period = timeSeries.take();
                var freeWorker = freeWorkers.take();
                executeTask(period, freeWorker);
            } catch (Exception ignored) {

            }
        }
    }

    public void addPeriod(ElasticTimeSeries period) {
        timeSeries.add(period);
    }

    private WorkerCallback getCallback() {
        return new WorkerCallback() {
            @Override
            public void onSuccess(CallbackSuccess success) {
                onSuccessWorker(success);
            }

            @Override
            public void onError(CallbackError error) {
                onErrorWorker(error);
            }
        };
    }

    private void onSuccessWorker(CallbackSuccess success) {
        var periodOptional = timeSeriesRepository.findById(success.periodId());
        if (periodOptional.isPresent()) {
            var period = periodOptional.get();
            period.setStatus(StatusWork.COMPLETE);
            timeSeriesRepository.save(period);
        }
        markWorkersIsFree(success.workerId());
    }

    private void onErrorWorker(CallbackError error) {
        var periodOptional = timeSeriesRepository.findById(error.idPeriod());
        if (periodOptional.isPresent()){
            var period = periodOptional.get();
            var lastProcessedEventTimestamp = error.lastProcessedTimestamp();
            period.setStatus(StatusWork.ERROR);
            period.setLastProcessedEvent(lastProcessedEventTimestamp);
            if(lastProcessedEventTimestamp != -1L) {
                period.setStartTime(lastProcessedEventTimestamp);
            }
            timeSeriesRepository.save(period);
        }
        markWorkersIsFree(error.workerId());
    }

    private void executeTask(ElasticTimeSeries period, int workerId) {
        var worker = workersInfo.get(workerId);
        if (worker == null) {
            LOG.error("Worker with {} id not exists", workerId);
            return;
        }
        workers.execute(() -> worker.processPeriod(period));
    }

    private void markWorkersIsFree(int workerId) {
        try {
            freeWorkers.put(workerId);
        } catch (Exception ignored) {

        }
    }

    @PreDestroy
    public void close() throws Exception {
        inProcess = false;
        workers.shutdown();
    }
}
