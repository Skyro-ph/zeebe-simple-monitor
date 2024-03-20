package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.AccessEntity;
import io.zeebe.monitor.querydsl.AccessEntityPredicatesBuilder;
import io.zeebe.monitor.repository.AccessEntityRepository;
import io.zeebe.monitor.rest.dto.UserAccessDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.querydsl.core.types.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminViewController extends AbstractViewController {
    @Autowired private AccessEntityRepository accessRepository;

    @GetMapping("/admin")
    public String adminPage(final Map<String, Object> model,
                            final Pageable pageable,
                            @RequestParam(required = false,name = "bpmnProcessId") String bpmnProcessId,
                            @RequestParam(required = false, name = "userId") String userId) {
        final Predicate predicate = new AccessEntityPredicatesBuilder()
                .withBpmnProcessId(bpmnProcessId)
                .withUserId(userId)
                .build();

        final Page<AccessEntity> accessEntities = accessRepository.findAll(predicate, pageable);
        final List<UserAccessDto> dtos = new ArrayList<>();
        for (final var entity : accessEntities) {
            final var dto = toDto(entity);
            dtos.add(dto);
        }
        model.put("usersAccesses", dtos);

        addPaginationToModel(model, pageable, accessEntities.getTotalElements());
        addDefaultAttributesToModel(model);
        return "admin-page-view";
    }

    static UserAccessDto toDto(final AccessEntity entity) {
        final var dto = new UserAccessDto();
        dto.setUserId(entity.getUserId());
        dto.setBpmnProcessId(entity.getBpmnProcessId());
        dto.setPermission(entity.getPermission().name());
        dto.setPermissionId(entity.getId());
        return dto;
    }
}
