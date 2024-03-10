package io.zeebe.monitor.repository;

import io.zeebe.monitor.entity.AccessEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessEntityRepository extends CrudRepository<AccessEntity, Long> {
    List<AccessEntity> getAllByUserId(String userId);
}
