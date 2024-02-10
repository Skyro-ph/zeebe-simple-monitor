package io.zeebe.monitor.zeebe.hazelcast.importers;

import com.google.protobuf.Message.Builder;

import java.util.Set;

public interface Importer<T> {
    void importData(T record);
    Set<String> supportedTypes();
    Builder getRecordBuilder();
}
