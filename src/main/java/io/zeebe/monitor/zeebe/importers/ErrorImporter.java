package io.zeebe.monitor.zeebe.importers;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.ErrorEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.ErrorRepository;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class ErrorImporter {

  @Autowired private ErrorRepository errorRepository;

  public void importError(final Schema.ErrorRecord record) {

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
