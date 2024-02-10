package io.zeebe.monitor.zeebe.elastic;

public record CallbackError(
        long idPeriod,
        int workerId,
        long lastProcessedTimestamp
) {
}
