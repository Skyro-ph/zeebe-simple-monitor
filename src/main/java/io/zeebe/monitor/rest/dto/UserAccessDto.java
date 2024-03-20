package io.zeebe.monitor.rest.dto;

public class UserAccessDto {
    private long permissionId;
    private String userId;
    private String bpmnProcessId;
    private String permission;

    public String getUserId() {
        return userId;
    }

    public String getBpmnProcessId() {
        return bpmnProcessId;
    }

    public String getPermission() {
        return permission;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(long permissionId) {
        this.permissionId = permissionId;
    }
}
