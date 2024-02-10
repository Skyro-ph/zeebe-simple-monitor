package io.zeebe.monitor.zeebe.hazelcast.importers;

import com.google.protobuf.Message;
import io.camunda.zeebe.protocol.record.intent.IncidentIntent;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.IncidentEntity;
import io.zeebe.monitor.repository.IncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class IncidentHazelcastImporter implements Importer<Schema.IncidentRecord> {

    @Autowired private IncidentRepository incidentRepository;

    @Override
    public Set<String> supportedTypes() {
        return Set.of("INCIDENT");
    }

    @Override
    public Message.Builder getRecordBuilder() {
        return Schema.IncidentRecord.newBuilder();
    }

    @Override
    public void importData(final Schema.IncidentRecord record) {

        final String intent = record.getMetadata().getIntent();
        final long key = record.getMetadata().getKey();
        final long timestamp = record.getMetadata().getTimestamp();

        final IncidentEntity entity =
                incidentRepository
                        .findById(key)
                        .orElseGet(
                                () -> {
                                    final IncidentEntity newEntity = new IncidentEntity();
                                    newEntity.setKey(key);
                                    newEntity.setBpmnProcessId(record.getBpmnProcessId());
                                    newEntity.setProcessDefinitionKey(record.getProcessDefinitionKey());
                                    newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
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
