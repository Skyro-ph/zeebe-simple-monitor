package io.zeebe.monitor.zeebe.elastic;

public interface WorkerCallback {
    void onSuccess(CallbackSuccess success);
    void onError(CallbackError error);
}
