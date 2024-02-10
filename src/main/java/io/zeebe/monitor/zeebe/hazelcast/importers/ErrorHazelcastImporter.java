package io.zeebe.monitor.zeebe.hazelcast.importers;

import com.google.protobuf.Message;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.ErrorEntity;
import io.zeebe.monitor.repository.ErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ErrorHazelcastImporter implements Importer<Schema.ErrorRecord> {

  @Autowired private ErrorRepository errorRepository;

    @Override
    public Set<String> supportedTypes() {
        return Set.of("ERROR");
    }

    @Override
    public Message.Builder getRecordBuilder() {
        return Schema.ErrorRecord.newBuilder();
    }

    @Override
  public void importData(final Schema.ErrorRecord record) {

    final var metadata = record.getMetadata();
    final var position = metadata.getPosition();

    final var entity =
        errorRepository
            .findById(position)
            .orElseGet(
                () -> {
                  final var newEntity = new ErrorEntity();
                  newEntity.setPosition(position);
                  newEntity.setErrorEventPosition(record.getErrorEventPosition());
                  newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
                  newEntity.setExceptionMessage(record.getExceptionMessage());
                  newEntity.setStacktrace(record.getStacktrace());
                  newEntity.setTimestamp(metadata.getTimestamp());
                  return newEntity;
                });

    errorRepository.save(entity);
  }
}
