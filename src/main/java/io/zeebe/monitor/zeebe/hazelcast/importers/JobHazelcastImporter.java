package io.zeebe.monitor.zeebe.hazelcast.importers;

import com.google.protobuf.Message;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.JobEntity;
import io.zeebe.monitor.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class JobHazelcastImporter implements Importer<Schema.JobRecord> {

  @Autowired private JobRepository jobRepository;

    @Override
    public Set<String> supportedTypes() {
        return Set.of("JOB");
    }

    @Override
    public Message.Builder getRecordBuilder() {
        return Schema.JobRecord.newBuilder();
    }

    @Override
  public void importData(final Schema.JobRecord record) {

    final String intent = record.getMetadata().getIntent();
    final long key = record.getMetadata().getKey();
    final long timestamp = record.getMetadata().getTimestamp();

    final JobEntity entity =
        jobRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final JobEntity newEntity = new JobEntity();
                  newEntity.setKey(key);
                  newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
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
