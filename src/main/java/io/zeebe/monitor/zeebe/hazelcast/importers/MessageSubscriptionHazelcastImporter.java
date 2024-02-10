package io.zeebe.monitor.zeebe.hazelcast.importers;

import com.google.protobuf.Message;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.MessageSubscriptionEntity;
import io.zeebe.monitor.repository.MessageSubscriptionRepository;

import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageSubscriptionHazelcastImporter implements Importer<Schema.MessageSubscriptionRecord> {

  @Autowired private MessageSubscriptionRepository messageSubscriptionRepository;

    @Override
    public Set<String> supportedTypes() {
        return Set.of("MESSAGE_SUBSCRIPTION", "PROCESS_MESSAGE_SUBSCRIPTION");
    }

    @Override
    public Message.Builder getRecordBuilder() {
        return Schema.MessageSubscriptionRecord.newBuilder();
    }

    @Override
    public void importData(final Schema.MessageSubscriptionRecord record) {

    final String intent = record.getMetadata().getIntent();
    final long timestamp = record.getMetadata().getTimestamp();

    final MessageSubscriptionEntity entity =
        messageSubscriptionRepository
            .findByElementInstanceKeyAndMessageName(
                record.getElementInstanceKey(), record.getMessageName())
            .orElseGet(
                () -> {
                  final MessageSubscriptionEntity newEntity = new MessageSubscriptionEntity();
                  newEntity.setId(
                      generateId()); // message subscription doesn't have a key - it is always '-1'
                  newEntity.setElementInstanceKey(record.getElementInstanceKey());
                  newEntity.setMessageName(record.getMessageName());
                  newEntity.setCorrelationKey(record.getCorrelationKey());
                  newEntity.setProcessInstanceKey(record.getProcessInstanceKey());
                  return newEntity;
                });

    entity.setState(intent.toLowerCase());
    entity.setTimestamp(timestamp);
    messageSubscriptionRepository.save(entity);
  }

  public void importMessageStartEventSubscription(
      final Schema.MessageStartEventSubscriptionRecord record) {

    final String intent = record.getMetadata().getIntent();
    final long timestamp = record.getMetadata().getTimestamp();

    final MessageSubscriptionEntity entity =
        messageSubscriptionRepository
            .findByProcessDefinitionKeyAndMessageName(
                record.getProcessDefinitionKey(), record.getMessageName())
            .orElseGet(
                () -> {
                  final MessageSubscriptionEntity newEntity = new MessageSubscriptionEntity();
                  newEntity.setId(
                      generateId()); // message subscription doesn't have a key - it is always '-1'
                  newEntity.setMessageName(record.getMessageName());
                  newEntity.setProcessDefinitionKey(record.getProcessDefinitionKey());
                  newEntity.setTargetFlowNodeId(record.getStartEventId());
                  return newEntity;
                });

    entity.setState(intent.toLowerCase());
    entity.setTimestamp(timestamp);
    messageSubscriptionRepository.save(entity);
  }

  private String generateId() {
    return UUID.randomUUID().toString();
  }
}
