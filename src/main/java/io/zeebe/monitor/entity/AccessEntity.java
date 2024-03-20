package io.zeebe.monitor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity(name = "ACCESS_ENTITY")
public class AccessEntity {
    @Id
    @GeneratedValue
    @Column(name = "ID_")
    private long id;

    @Column(name = "USER_ID_")
    private String userId;

    @Column(name = "BPMN_PROCESS_ID_")
    private String bpmnProcessId;

    @Enumerated(EnumType.STRING)
    @Column(name = "PERMISSION_")
    private Permission permission;

    public enum Permission {
        VIEW,
        EDIT,
        ;
    }

    public long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Permission getPermission() {
        return permission;
    }

    public String getBpmnProcessId() {
        return bpmnProcessId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public void setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
    }
}
