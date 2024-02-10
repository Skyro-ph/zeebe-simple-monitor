package io.zeebe.monitor.zeebe.hazelcast.importers;

import com.google.protobuf.Message;
import io.zeebe.exporter.proto.Schema;
import io.zeebe.monitor.entity.MessageEntity;
import io.zeebe.monitor.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MessageHazelcastImporter implements Importer<Schema.MessageRecord> {

  @Autowired private MessageRepository messageRepository;

    @Override
    public Set<String> supportedTypes() {
        return Set.of("MESSAGE");
    }

    @Override
    public Message.Builder getRecordBuilder() {
        return Schema.MessageRecord.newBuilder();
    }

    @Override
  public void importData(final Schema.MessageRecord record) {

    final String intent = record.getMetadata().getIntent();
    final long key = record.getMetadata().getKey();
    final long timestamp = record.getMetadata().getTimestamp();

    final MessageEntity entity =
        messageRepository
            .findById(key)
            .orElseGet(
                () -> {
                  final MessageEntity newEntity = new MessageEntity();
                  newEntity.setKey(key);
                  newEntity.setName(record.getName());
                  newEntity.setCorrelationKey(record.getCorrelationKey());
                  newEntity.setMessageId(record.getMessageId());
                  newEntity.setPayload(record.getVariables().toString());
                  return newEntity;
                });

    entity.setState(intent.toLowerCase());
    entity.setTimestamp(timestamp);
    messageRepository.save(entity);
  }
}
