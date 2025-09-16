package com.act.ecommerce.entity;


public class CronUpdateRequest {
    private String taskName;
    private String cronExpression;

    // Getters and Setters
    public String getTaskName() {
        return taskName;
    }
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    public String getCronExpression() {
        return cronExpression;
    }
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
}