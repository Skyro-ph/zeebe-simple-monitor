package io.zeebe.monitor.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import io.zeebe.monitor.entity.AccessEntity;

import java.util.ArrayList;
import java.util.List;

public class AccessEntityPredicatesBuilder {
  private final List<Predicate> predicates = new ArrayList<>();
  private final PathBuilder<AccessEntity> pathBuilder = new PathBuilder<>(AccessEntity.class, "accessEntity");

    public AccessEntityPredicatesBuilder withBpmnProcessId(String bpmnProcessId) {
        if (bpmnProcessId != null) {
            predicates.add(pathBuilder.getString("bpmnProcessId").containsIgnoreCase(bpmnProcessId));
        }
        return this;
    }

    public AccessEntityPredicatesBuilder withUserId(String userId) {
        if (userId != null) {
            predicates.add(pathBuilder.getString("userId").containsIgnoreCase(userId));
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
}
