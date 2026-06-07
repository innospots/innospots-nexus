package com.innospots.nexus.base.domain.project;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * A project within an organization. Projects scope workflows, resources,
 * and execution records.
 */
public class ProjectInfo {

    private final Long projectId;
    private final String projectName;
    private final String projectCode;
    private Long organizationId;
    private String description;
    private BasicStatus status = BasicStatus.ENABLED;

    private ProjectInfo(Long projectId, String projectName, String projectCode) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectCode = projectCode;
    }

    public static ProjectInfo named(Long projectId, String projectName, String projectCode) {
        return new ProjectInfo(projectId, projectName, projectCode);
    }

    public Long projectId() {
        return projectId;
    }

    public String projectName() {
        return projectName;
    }

    public String projectCode() {
        return projectCode;
    }

    public Long organizationId() {
        return organizationId;
    }

    public ProjectInfo organizationId(Long organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    public String description() {
        return description;
    }

    public ProjectInfo description(String description) {
        this.description = description;
        return this;
    }

    public BasicStatus status() {
        return status;
    }

    public ProjectInfo status(BasicStatus status) {
        this.status = status == null ? BasicStatus.ENABLED : status;
        return this;
    }
}
