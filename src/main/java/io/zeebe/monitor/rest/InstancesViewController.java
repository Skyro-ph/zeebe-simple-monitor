package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ProcessInstanceEntity;
import io.zeebe.monitor.repository.ProcessInstanceRepository;
import io.zeebe.monitor.rest.dto.ProcessInstanceListDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.zeebe.monitor.security.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InstancesViewController extends AbstractViewController {

  @Autowired protected ProcessInstanceRepository processInstanceRepository;
  @Autowired protected PermissionService permissionService;

  @GetMapping("/views/instances")
  public String instanceList(final Map<String, Object> model, final Pageable pageable) {
    var availableIds = permissionService.getAllAvailableId();

    final List<ProcessInstanceListDto> instances = new ArrayList<>();
    var dtos = processInstanceRepository.findByBpmnProcessIdIn(availableIds, pageable);
    if (dtos == null) dtos = Page.empty();
    for (final ProcessInstanceEntity instanceEntity : dtos) {
      final ProcessInstanceListDto dto = ProcessesViewController.toDto(instanceEntity);
      instances.add(dto);
    }

    model.put("instances", instances);
    model.put("count", dtos.getTotalElements());

    addPaginationToModel(model, pageable, dtos.getTotalElements());
    addDefaultAttributesToModel(model);

    return "instance-list-view";
  }
}
