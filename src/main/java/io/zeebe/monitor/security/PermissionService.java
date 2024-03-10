package io.zeebe.monitor.security;

import io.zeebe.monitor.entity.AccessEntity;
import io.zeebe.monitor.repository.AccessEntityRepository;
import io.zeebe.monitor.security.entity.TokenUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionService {
    private final AccessEntityRepository accessEntityRepository;

    public PermissionService(AccessEntityRepository accessEntityRepository) {
        this.accessEntityRepository = accessEntityRepository;
    }

    public List<String> getAllAvailableId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof TokenUser principal) {
            return accessEntityRepository.getAllByUserId(principal.getUsername())
                    .stream()
                    .map(AccessEntity::getBpmnProcessId)
                    .toList();
        }

        return List.of();
    }
}
