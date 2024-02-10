package io.zeebe.monitor.zeebe.hazelcast.importers;

import com.google.protobuf.Message;
import io.camunda.zeebe.protocol.Protocol;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.ProcessEntity;
import io.zeebe.monitor.repository.ProcessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ProcessHazelcastImporter implements Importer<Schema.ProcessRecord> {

  @Autowired private ProcessRepository processRepository;

  @Override
  public Set<String> supportedTypes() {
    return Set.of("PROCESS");
  }

  @Override
  public Message.Builder getRecordBuilder() {
    return Schema.ProcessRecord.newBuilder();
  }

  @Override
  public void importData(final Schema.ProcessRecord record) {
    final int partitionId = record.getMetadata().getPartitionId();

    if (partitionId != Protocol.DEPLOYMENT_PARTITION) {
      // ignore process event on other partitions to avoid duplicates
      return;
    }

    final ProcessEntity entity = new ProcessEntity();
    entity.setKey(record.getProcessDefinitionKey());
    entity.setBpmnProcessId(record.getBpmnProcessId());
    entity.setVersion(record.getVersion());
    entity.setResource(record.getResource().toStringUtf8());
    entity.setTimestamp(record.getMetadata().getTimestamp());
    processRepository.save(entity);
  }
}
