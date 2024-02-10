package io.zeebe.monitor.zeebe.hazelcast.importers;

import com.google.protobuf.Message;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.VariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class VariableHazelcastImporter implements Importer<Schema.VariableRecord> {

  @Autowired private VariableRepository variableRepository;

  @Override
  public Set<String> supportedTypes() {
    return Set.of("VARIABLE");
  }

  @Override
  public Message.Builder getRecordBuilder() {
    return Schema.VariableRecord.newBuilder();
  }

  @Override
  public void importData(final Schema.VariableRecord record) {
    final VariableEntity newVariable = new VariableEntity();
    newVariable.setPosition(record.getMetadata().getPosition());
    newVariable.setPartitionId(record.getMetadata().getPartitionId());

    if (!variableRepository.existsById(newVariable.getGeneratedIdentifier())) {
      newVariable.setTimestamp(record.getMetadata().getTimestamp());
      newVariable.setProcessInstanceKey(record.getProcessInstanceKey());
      newVariable.setName(record.getName());
      newVariable.setValue(record.getValue());
      newVariable.setScopeKey(record.getScopeKey());
      newVariable.setState(record.getMetadata().getIntent().toLowerCase());
      variableRepository.save(newVariable);
    }
  }
}
