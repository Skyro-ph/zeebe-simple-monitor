package io.zeebe.monitor.zeebe.importers;

import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.IncidentRepository;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class IncidentImporter {

  @Autowired private IncidentRepository incidentRepository;
  @Autowired private ProcessInstanceRepository processInstanceRepository;

  public void importIncident(final Schema.IncidentRecord record) {

    final String intent = record.getMetadata().getIntent();
    final long key = record.getMetadata().getKey();
    final long timestamp = record.getMetadata().getTimestamp();
    final ProcessInstanceEntity processInstance = processInstanceRepository.findById(record.getProcessInstanceKey())
          .orElseThrow(
                  () -> new ResponseStatusException(NOT_FOUND, "No process instance found with key: " + record.getProcessInstanceKey())
          );

    final IncidentEntity entity =
        incidentRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final IncidentEntity newEntity = new IncidentEntity();
                  newEntity.setKey(key);
                  newEntity.setBpmnProcessId(record.getBpmnProcessId());
                  newEntity.setProcessDefinitionKey(record.getProcessDefinitionKey());
                  newEntity.setProcessInstance(processInstance);
                  newEntity.setElementInstanceKey(record.getElementInstanceKey());
                  newEntity.setJobKey(record.getJobKey());
                  newEntity.setErrorType(record.getErrorType());
                  newEntity.setErrorMessage(record.getErrorMessage());
                  return newEntity;
                });

    if (intent.equalsIgnoreCase(IncidentIntent.CREATED.name())) {
      entity.setCreated(timestamp);
      incidentRepository.save(entity);

    } else if (intent.equalsIgnoreCase(IncidentIntent.RESOLVED.name())) {
      entity.setResolved(timestamp);
      incidentRepository.save(entity);
    }
  }

}
