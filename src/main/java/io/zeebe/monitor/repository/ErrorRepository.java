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

import io.zeebe.monitor.entity.ErrorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ErrorRepository extends PagingAndSortingRepository<ErrorEntity, Long> {

  Page<ErrorEntity> findByProcessInstanceKey(long processInstanceKey, Pageable pageable);

  long countByProcessInstanceKey(long processInstanceKey);

  @Async
  @Transactional
  @Modifying
  CompletableFuture<Void> deleteByProcessInstanceKeyIn(Collection<Long> processInstanceKeys);
}
