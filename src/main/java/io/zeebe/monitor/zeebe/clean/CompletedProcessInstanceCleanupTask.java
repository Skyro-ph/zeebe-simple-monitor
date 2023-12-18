package io.zeebe.monitor.zeebe.clean;

import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CompletedProcessInstanceCleanupTask {
    private static final Logger LOG = LoggerFactory.getLogger(CompletedProcessInstanceCleanupTask.class);
    private final ProcessInstanceRepository processInstanceRepository;

    private final long expirationIntervalMillis;

    public CompletedProcessInstanceCleanupTask(@Value("${cleanup.processInstances.completed.expiration-days}") final long expirationIntervalInDays,
                                               ProcessInstanceRepository processInstanceRepository) {
        this.expirationIntervalMillis = TimeUnit.DAYS.toMillis(expirationIntervalInDays);
        this.processInstanceRepository = processInstanceRepository;
    }

    @Scheduled(fixedRateString = "${cleanup.processInstances.completed.frequency-days}", timeUnit = TimeUnit.DAYS)
    public void cleanupExpiredCompletedProcesses() {
        var idsToClean = processInstanceRepository.findAllExpiredIds(
                ProcessInstanceEntity.State.COMPLETED.toString(),
                System.currentTimeMillis(),
                expirationIntervalMillis
        );

        LOG.info("Cleaning expired process instances with ids: " + idsToClean);

        processInstanceRepository.deleteAllById(idsToClean);
    }
}