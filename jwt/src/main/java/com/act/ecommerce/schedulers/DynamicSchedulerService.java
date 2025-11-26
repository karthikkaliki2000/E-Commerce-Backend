package com.act.ecommerce.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class DynamicSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicSchedulerService.class);

    private final TaskScheduler taskScheduler;

    // Map to hold task references
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    // Map to hold cron expressions for restart
    private final Map<String, String> taskCrons = new ConcurrentHashMap<>();

    public DynamicSchedulerService(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    /**
     * Initialize all tasks on startup
     */
    @PostConstruct
    public void initializeAllTasks() {
        logger.info("Initializing scheduled tasks...");
        taskCrons.forEach((taskName, cron) -> {
            try {
                restartScheduledTask(taskName, cron);
            } catch (Exception e) {
                logger.error("Failed to initialize task '{}': {}", taskName, e.getMessage());
            }
        });
    }

    /**
     * Update cron expression for a task
     */
    public void updateCronExpression(String taskName, String cronExpression) {
        validateCron(cronExpression);
        stopTask(taskName);
        taskCrons.put(taskName, cronExpression);
        restartScheduledTask(taskName, cronExpression);
        logger.info("Cron updated for task '{}': {}", taskName, cronExpression);
    }

    /**
     * Stop a scheduled task
     */
    public void stopTask(String taskName) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskName);
        if (future != null) {
            future.cancel(false);
            logger.info("Task '{}' stopped.", taskName);
        } else {
            logger.warn("No active task found to stop: '{}'", taskName);
        }
    }

    /**
     * Check if a task is currently running
     */
    public boolean isTaskRunning(String taskName) {
        ScheduledFuture<?> future = scheduledTasks.get(taskName);
        return future != null && !future.isCancelled();
    }

    /**
     * Restart a scheduled task with new cron
     */
    public void restartScheduledTask(String taskName, String cronExpression) {
        validateCron(cronExpression);
        Runnable task = getRunnableForTask(taskName);
        CronTrigger trigger = new CronTrigger(cronExpression);
        ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);
        scheduledTasks.put(taskName, future);
        logger.info("Task '{}' scheduled with cron '{}'", taskName, cronExpression);
    }

    /**
     * Validate cron expression
     */
    private void validateCron(String cronExpression) {
        CronExpression.parse(cronExpression); // throws IllegalArgumentException if invalid
    }

    /**
     * Provide task logic based on task name
     */
    private Runnable getRunnableForTask(String taskName) {
        // Replace with actual logic per task
        return () -> logger.info("Executing task '{}'", taskName);
    }

    public Map<String, String> getAllCronExpressions() {
        return Collections.unmodifiableMap(taskCrons);
    }

}
