package io.zeebe.monitor.zeebe.elastic;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.zeebe.monitor.entity.ElasticTimeSeries;
import io.zeebe.monitor.repository.ElasticTimeSeriesRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "zeebe-importer", havingValue = "elastic")
public class ZeebeImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ZeebeImportService.class);
    private static final long DELTA = 60L;

    @Value("${elastic.index}")
    private String elasticIndex;

    private final ElasticClientProvider clientProvider;
    private final ElasticTimeSeriesRepository timeSeriesRepository;
    private final ZeebeElasticWorkerGroup workerGroup;

    private long firstEventTimestamp = -1L;
    private boolean inProcess = true;

    public ZeebeImportService(ElasticClientProvider clientProvider, ElasticTimeSeriesRepository timeSeriesRepository, ZeebeElasticWorkerGroup workerGroup) {
        this.clientProvider = clientProvider;
        this.timeSeriesRepository = timeSeriesRepository;
        this.workerGroup = workerGroup;
        init();
    }

    private void init() {
        if(firstEventTimestamp != -1) {
            LOG.error("Class is already initialized");
            return;
        }
        try {
            firstEventTimestamp = getFirstEventTimestamp();
            while (firstEventTimestamp == -1) {
                init();
            }
        } catch (Exception e) {
            LOG.error("Error connect to Elasticsearch", e);
        }
    }

    @Scheduled(initialDelay = 1000 * 30, fixedDelay=Long.MAX_VALUE, timeUnit = TimeUnit.NANOSECONDS )
    public void startWork() {
        while (inProcess) {
            var nextPeriodOptional = getNextPeriodIfExist();
            if(nextPeriodOptional.isPresent()) {
                var nextPeriod = nextPeriodOptional.get();
                workerGroup.addPeriod(nextPeriod);
            }
        }
    }

    private long getFirstEventTimestamp() throws IOException {
        var client = clientProvider.getClient();
        var searchRequest = new SearchRequest.Builder()
                .index(elasticIndex)
                .sort(s -> s.field(f -> f.field("timestamp")))
                .size(1)
                .build();

        var firstEvent = client.client().search(searchRequest, ObjectNode.class);
        client.transport().close();

        return firstEvent.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .mapToLong(r -> r.get("timestamp").asLong())
                .max()
                .orElse(-1L);
    }

    private Optional<ElasticTimeSeries> getNextPeriodIfExist() {
        return timeSeriesRepository.createNextTimeSeriesIfPossible(DELTA, firstEventTimestamp);
    }

    @PreDestroy
    public void close() throws Exception {
        inProcess = false;
    }
}
