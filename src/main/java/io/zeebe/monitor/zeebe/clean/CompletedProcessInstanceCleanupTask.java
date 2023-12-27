package io.zeebe.monitor.zeebe.clean;

import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class CompletedProcessInstanceCleanupTask {
    private static final Logger LOG = LoggerFactory.getLogger(CompletedProcessInstanceCleanupTask.class);
    private final ProcessInstanceRepository processInstanceRepository;
    private final ElementInstanceRepository elementInstanceRepository;
    private final ErrorRepository errorRepository;
    private final IncidentRepository incidentRepository;
    private final JobRepository jobRepository;
    private final MessageSubscriptionRepository messageSubscriptionRepository;
    private final TimerRepository timerRepository;
    private final VariableRepository variableRepository;


    private final long expirationIntervalMillis;

    public CompletedProcessInstanceCleanupTask(@Value("${cleanup.processInstances.completed.expiration-days}") final long expirationIntervalInDays,
                                               ProcessInstanceRepository processInstanceRepository,
                                               ElementInstanceRepository elementInstanceRepository,
                                               ErrorRepository errorRepository,
                                               IncidentRepository incidentRepository,
                                               JobRepository jobRepository,
                                               MessageSubscriptionRepository messageSubscriptionRepository,
                                               TimerRepository timerRepository,
                                               VariableRepository variableRepository) {
        this.expirationIntervalMillis = TimeUnit.DAYS.toMillis(expirationIntervalInDays);
        this.processInstanceRepository = processInstanceRepository;
        this.elementInstanceRepository = elementInstanceRepository;
        this.errorRepository = errorRepository;
        this.incidentRepository = incidentRepository;
        this.jobRepository = jobRepository;
        this.messageSubscriptionRepository = messageSubscriptionRepository;
        this.timerRepository = timerRepository;
        this.variableRepository = variableRepository;
    }

    @Scheduled(fixedRateString = "${cleanup.processInstances.completed.frequency-days}", timeUnit = TimeUnit.DAYS)
    public void cleanupExpiredData() {
        var idsToClean = processInstanceRepository.findAllExpiredIds(
                ProcessInstanceEntity.State.COMPLETED.toString(),
                System.currentTimeMillis(),
                expirationIntervalMillis
        );

        LOG.info("Cleaning expired process instances with ids: " + idsToClean);

        cleanupData(idsToClean);
    }

    private void cleanupData(Iterable<Long> processInstanceIdsToClean) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        futures.add(errorRepository.deleteByProcessInstanceKeysAsync(processInstanceIdsToClean));
        futures.add(incidentRepository.deleteByProcessInstanceKeysAsync(processInstanceIdsToClean));
        futures.add(jobRepository.deleteByProcessInstanceKeysAsync(processInstanceIdsToClean));
        futures.add(messageSubscriptionRepository.deleteByProcessInstanceKeysAsync(processInstanceIdsToClean));
        futures.add(processInstanceRepository.deleteByProcessInstanceKeysAsync(processInstanceIdsToClean));
        futures.add(timerRepository.deleteByProcessInstanceKeysAsync(processInstanceIdsToClean));
        futures.add(variableRepository.deleteByProcessInstanceKeysAsync(processInstanceIdsToClean));
        futures.add(elementInstanceRepository.deleteByProcessInstanceKeysAsync(processInstanceIdsToClean));

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        combinedFuture.join();
    }
}
