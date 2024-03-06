package io.zeebe.monitor.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import io.zeebe.monitor.entity.QProcessInstanceEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class InstancesEntityPredicatesBuilder {
    final PathBuilder<QProcessInstanceEntity> pathBuilder = new PathBuilder<>(QProcessInstanceEntity.class, QProcessInstanceEntity.processInstanceEntity.getMetadata());
    private final List<Predicate> predicates = new ArrayList<>();

    public InstancesEntityPredicatesBuilder withProcessInstanceKey(String processInstanceKey) {
        if (!isEmpty(processInstanceKey)) {
            var value = parseLong(processInstanceKey);
            value.ifPresent(val -> predicates.add(pathBuilder.getNumber("key", Long.class).eq(val)));
        }
        return this;
    }

    public InstancesEntityPredicatesBuilder createdAfter(String timestamp) {
        if (!isEmpty(timestamp)) {
            final Optional<Long> created = parseIsoToUtcMillis(timestamp);
            created.ifPresent(utcMillis -> predicates.add(pathBuilder.getNumber("start", Long.class).goe(utcMillis)));
        }
        return this;
    }

    public InstancesEntityPredicatesBuilder createdBefore(String timestamp) {
        if (!isEmpty(timestamp)) {
            final Optional<Long> created = parseIsoToUtcMillis(timestamp);
            created.ifPresent(utcMillis -> predicates.add(pathBuilder.getNumber("end", Long.class).loe(utcMillis)));
        }
        return this;
    }

    public InstancesEntityPredicatesBuilder withStateType(String state) {
        if (!isEmpty(state)) {
            predicates.add(pathBuilder.getString("state").containsIgnoreCase(state));
        }
        return this;
    }

    public Predicate build() {
        BooleanExpression result = Expressions.asBoolean(true).isTrue();
        for (Predicate predicate : predicates) {
            result = result.and(predicate);
        }
        return result;
    }

    private Optional<Long> parseIsoToUtcMillis(String timestamp) {
        try {
            final ZonedDateTime zonedDateTime = ZonedDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(timestamp));
            final long utcMillis = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toInstant().toEpochMilli();
            return Optional.of(utcMillis);
        } catch (DateTimeParseException ignore) {
            // ignore
        }
        return Optional.empty();
    }

    private Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException ignore) {

        }
        return Optional.empty();
    }
}
