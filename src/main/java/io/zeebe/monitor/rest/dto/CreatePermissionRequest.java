package io.zeebe.monitor.rest.dto;

public record CreatePermissionRequest(
    String userId,
    String permission,
    String bpmnProcessId
) {
}
