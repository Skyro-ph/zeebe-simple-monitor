package io.zeebe.monitor.repository;

import io.zeebe.monitor.entity.AccessEntity;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessEntityRepository extends CrudRepository<AccessEntity, Long>, QuerydslPredicateExecutor<AccessEntity> {
    List<AccessEntity> getAllByUserId(String userId);
    List<AccessEntity> getAllByUserIdAndPermission(String userId, AccessEntity.Permission permission);
    List<AccessEntity> findByUserIdAndBpmnProcessIdAndPermission(String userId, String bpmnProcessId, AccessEntity.Permission permission);
}
