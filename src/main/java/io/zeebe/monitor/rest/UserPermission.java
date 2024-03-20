package io.zeebe.monitor.rest;

import io.zeebe.monitor.public_interface.CreatePermissionDto;
import io.zeebe.monitor.rest.dto.CreatePermissionRequest;
import io.zeebe.monitor.security.PermissionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/permissions")
public class UserPermission {
    private final PermissionService permissionService;

    public UserPermission(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public void addPermission(@RequestBody CreatePermissionRequest request) {
        var dto = new CreatePermissionDto(
                request.userId(),
                request.permission(),
                request.bpmnProcessId()
        );
        permissionService.addPermission(dto);
    }

    @DeleteMapping("/{permissionId}")
    public void deletePermission(@PathVariable final long permissionId) {
        permissionService.deletePermission(permissionId);
    }
}
