package io.zeebe.monitor.zeebe.importers;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.JobEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.JobRepository;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobImporter {

  @Autowired private JobRepository jobRepository;
  @Autowired private ProcessInstanceRepository processInstanceRepository;

  public void importJob(final Schema.JobRecord record) {

    final String intent = record.getMetadata().getIntent();
    final long key = record.getMetadata().getKey();
    final long timestamp = record.getMetadata().getTimestamp();
    final ProcessInstanceEntity processInstance = processInstanceRepository.findById(record.getProcessInstanceKey())
            .orElseGet(() -> {
                ProcessInstanceEntity processInstanceEntity = new ProcessInstanceEntity();
                processInstanceEntity.setKey(record.getProcessInstanceKey());
                return processInstanceRepository.save(processInstanceEntity);
            });
    final JobEntity entity =
        jobRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final JobEntity newEntity = new JobEntity();
                  newEntity.setKey(key);
                  newEntity.setProcessInstance(processInstance);
                  newEntity.setElementInstanceKey(record.getElementInstanceKey());
                  newEntity.setJobType(record.getType());
                  return newEntity;
                });

    entity.setState(intent.toLowerCase());
    entity.setTimestamp(timestamp);
    entity.setWorker(record.getWorker());
    entity.setRetries(record.getRetries());
    jobRepository.save(entity);
  }

}
