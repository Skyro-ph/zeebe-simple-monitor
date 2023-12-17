package io.zeebe.monitor.zeebe.importers;

import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.entity.VariableEntity;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.repository.VariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class VariableImporter {

  @Autowired
  private VariableRepository variableRepository;
  @Autowired
  private ProcessInstanceRepository processInstanceRepository;

  public void importVariable(final Schema.VariableRecord record) {
    final VariableEntity newVariable = new VariableEntity();
    newVariable.setPosition(record.getMetadata().getPosition());
    newVariable.setPartitionId(record.getMetadata().getPartitionId());
    final ProcessInstanceEntity processInstance = processInstanceRepository.findById(record.getProcessInstanceKey())
            .orElseThrow(
                    () -> new ResponseStatusException(NOT_FOUND, "No process instance found with key: " + record.getProcessInstanceKey())
            );
    if (!variableRepository.existsById(newVariable.getGeneratedIdentifier())) {
      newVariable.setTimestamp(record.getMetadata().getTimestamp());
      newVariable.setProcessInstance(processInstance);
      newVariable.setName(record.getName());
      newVariable.setValue(record.getValue());
      newVariable.setScopeKey(record.getScopeKey());
      newVariable.setState(record.getMetadata().getIntent().toLowerCase());
      variableRepository.save(newVariable);
    }
  }

}
