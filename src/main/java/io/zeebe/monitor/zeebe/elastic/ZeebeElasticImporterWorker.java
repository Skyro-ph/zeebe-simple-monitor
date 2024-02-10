package io.zeebe.monitor.zeebe.elastic;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.zeebe.monitor.entity.ElasticTimeSeries;
import io.zeebe.monitor.zeebe.hazelcast.importers.ImporterFactory;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ZeebeElasticImporterWorker {
    private static final Logger LOG = LoggerFactory.getLogger(ZeebeElasticImporterWorker.class);
    private static final int BATCH_SIZE = 1_000;

    private final int workerId;
    private final ElasticClient client;
    private final WorkerCallback callback;
    private final String elasticIndex;
    private final ImporterFactory importerFactory;

    private long lastProcessedTimestamp = -1L;
    private WorkerStatus status = WorkerStatus.WAIT;

    public ZeebeElasticImporterWorker(int workerId, ElasticClient client, WorkerCallback callback, String elasticIndex, ImporterFactory importerFactory) {
        this.workerId = workerId;
        this.client = client;
        this.callback = callback;
        this.elasticIndex = elasticIndex;
        this.importerFactory = importerFactory;
    }

    public WorkerStatus getStatus() {
        return status;
    }

    public void processPeriod(ElasticTimeSeries period) {
        status = WorkerStatus.IN_PROCESS;
        lastProcessedTimestamp = -1L;

        try {
            var countRecord = processElasticData(period.getStartTime(), period.getEndTime());

            status = WorkerStatus.WAIT;
            var success = new CallbackSuccess(period.getId(), workerId, countRecord);
            callback.onSuccess(success);
        } catch (Exception e) {
            LOG.error("Error while importing.", e);

            status = WorkerStatus.WAIT;
            var error = new CallbackError(period.getId(), workerId, lastProcessedTimestamp);
            callback.onError(error);
        }
    }

    private int processElasticData(long startTime, long endTime) throws IOException {
        logStartImportInterval(startTime, endTime);
        AtomicInteger counter = new AtomicInteger(0);

        var searchResponse = client.client().search(
                getRequestBuilder(startTime, endTime).build(),
                ObjectNode.class);
        var hits = searchResponse.hits();

        while (true) {
            if (hits.hits().isEmpty()) {
                break;
            }

            var processedCount = hits.hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(r -> importRecord(r, counter))
                    .count();

            if (processedCount < BATCH_SIZE) {
                break;
            }

            var lastSortValues = hits.hits().get(hits.hits().size()).sort();
            searchResponse = client.client().search(
                    getRequestBuilder(startTime, endTime)
                            .searchAfter(lastSortValues).build(),
                    ObjectNode.class);
            hits = searchResponse.hits();
        }

        var latestTimestamp = hits.hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .mapToLong(r -> r.get("timestamp").asLong())
                .max()
                .orElse(-1L);
        logFinishImportInterval(counter.get(), latestTimestamp);

        return counter.get();
    }

    private SearchRequest.Builder getRequestBuilder(long startTime, long endTime) {
        return new SearchRequest.Builder()
                .index(elasticIndex)
                .query(q -> q.range(
                        rb -> rb.field("timestamp")
                                .gte(JsonData.of(startTime))
                                .lte(JsonData.of(endTime))
                )).sort(s -> s.field(f -> f.field("timestamp")))
                .size(BATCH_SIZE);
    }

    private long importRecord(ObjectNode record, AtomicInteger counter) {
        String type = record.get("valueType").asText();

        var importerOptional = importerFactory.getImporter(type);
        if (importerOptional.isPresent()) {
            var importer = importerOptional.get();
            var builder = importer.getRecordBuilder();
            parseJsonToProto(record, builder);
            importer.importData(builder.build());
        }

        counter.incrementAndGet();
        var timestamp = record.get("timestamp").asLong();
        lastProcessedTimestamp = timestamp;
        return timestamp;
    }

    private void parseJsonToProto(ObjectNode record, Message.Builder builder) {
        var metadata = record.deepCopy();
        metadata.remove("value");
        var value = record.with("value");
        value.set("metadata", metadata);

        try {
            JsonFormat.parser().ignoringUnknownFields().merge(value.toString(), builder);
        } catch (InvalidProtocolBufferException ex) {
            throw new RuntimeException("Error on converting json to protobuf", ex);
        }
    }

    private void logStartImportInterval(long startTime, long endTime) {
        LOG.info("Worker {}, start importing interval {} - {}",
                workerId,
                getZonedDateTime(startTime),
                getZonedDateTime(endTime)
        );
    }

    private void logFinishImportInterval(int countRecords, long latestRecordTime) {
        LOG.info("Imported {} records from elastic till {}",
                countRecords,
                getZonedDateTime(latestRecordTime)
        );
    }

    private ZonedDateTime getZonedDateTime(long timestamp) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    @PreDestroy
    public void close() throws Exception {
        client.transport().close();
    }
}
