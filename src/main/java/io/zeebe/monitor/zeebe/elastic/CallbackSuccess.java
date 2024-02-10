package io.zeebe.monitor.zeebe.elastic;

public record CallbackSuccess(
        long periodId,
        int workerId,
        int countRecord
) {
}
