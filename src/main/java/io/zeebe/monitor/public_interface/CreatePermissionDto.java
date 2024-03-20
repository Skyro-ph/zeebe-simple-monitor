package io.zeebe.monitor.public_interface;

public record CreatePermissionDto(
    String userId,
    String permission,
    String bpmnProcessId
) {
}
