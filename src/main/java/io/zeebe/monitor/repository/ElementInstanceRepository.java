/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.monitor.repository;

import io.zeebe.monitor.entity.ElementInstanceEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

public interface ElementInstanceRepository extends CrudRepository<ElementInstanceEntity, String> {

  Iterable<ElementInstanceEntity> findByProcessInstanceKey(long processInstanceKey);

  @Async
  @Modifying
  @Transactional
  @Query("DELETE FROM ELEMENT_INSTANCE e WHERE e.processInstanceKey IN :processInstanceKeys")
  CompletableFuture<Void> deleteByProcessInstanceKeysAsync(@Param("processInstanceKeys") Iterable<Long> processInstanceKeys);
}
