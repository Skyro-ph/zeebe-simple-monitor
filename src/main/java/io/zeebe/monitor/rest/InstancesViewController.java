package io.zeebe.monitor.rest;

import com.querydsl.core.types.Predicate;
import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.querydsl.InstancesEntityPredicatesBuilder;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.rest.dto.ProcessInstanceListDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InstancesViewController extends AbstractViewController {

  @Autowired protected ProcessInstanceRepository processInstanceRepository;

  @GetMapping("/views/instances")
  public String instanceList(final Map<String, Object> model,
                             final Pageable pageable,
                             @RequestParam(required = false,name = "processInstanceKey") String processInstanceKey,
                             @RequestParam(required = false, name = "createdAfter") String createdAfter,
                             @RequestParam(required = false, name = "createdBefore") String createdBefore,
                             @RequestParam(required = false, name = "stateType") String state) {

    final Predicate predicate = new InstancesEntityPredicatesBuilder()
            .withProcessInstanceKey(processInstanceKey)
            .createdAfter(createdAfter)
            .createdBefore(createdBefore)
            .withStateType(state)
            .build();

    final Page<ProcessInstanceEntity> entities = processInstanceRepository.findAll(predicate, pageable);
    final List<ProcessInstanceListDto> instances = new ArrayList<>();
    for (final ProcessInstanceEntity instanceEntity : entities) {
      final ProcessInstanceListDto dto = ProcessesViewController.toDto(instanceEntity);
      instances.add(dto);
    }

    final long count = entities.getTotalElements();

    model.put("instances", instances);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);

    return "instance-list-view";
  }
}
