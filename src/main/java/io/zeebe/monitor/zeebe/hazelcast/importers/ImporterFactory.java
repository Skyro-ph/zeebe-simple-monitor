package io.zeebe.monitor.zeebe.hazelcast.importers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ImporterFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ImporterFactory.class);
    private static final Map<String, Importer> IMPORTER_MAP = new HashMap<>();

    @Autowired
    private ImporterFactory(List<Importer<?>> importers) {
        for(Importer<?> importer : importers) {
            for(String type : importer.supportedTypes()) {
                IMPORTER_MAP.put(type, importer);
            }
        }
    }

    public <T> Optional<Importer<T>> getImporter(String type) {
        final Importer<T> importer = IMPORTER_MAP.get(type);
        if (importer == null) {
            LOG.error("Unknown type {}", type);
        }
        return Optional.ofNullable(importer);
    }
}
