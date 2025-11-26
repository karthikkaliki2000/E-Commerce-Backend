package com.act.ecommerce.controller;

import com.act.ecommerce.entity.CronUpdateRequest;
import com.act.ecommerce.schedulers.DynamicSchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerController.class);

    @Autowired
    private DynamicSchedulerService dynamicSchedulerService;



    /**
     * Dynamically update cron expression for any task
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/update-cron")
    public ResponseEntity<String> updateCron(@RequestBody CronUpdateRequest request) {
        try {
            String cronExpression = request.getCronExpression();
            if (cronExpression.trim().split("\\s+").length == 5) {
                cronExpression = "0 " + cronExpression;
            }
            request.setCronExpression(cronExpression);

            logger.info("Updating cron for task '{}': {}", request.getTaskName(), cronExpression);
            dynamicSchedulerService.updateCronExpression(request.getTaskName(), cronExpression);
            return ResponseEntity.ok("✅ Cron expression for task '" + request.getTaskName() + "' updated successfully!");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid cron expression for task '{}': {}", request.getTaskName(), e.getMessage());
            return ResponseEntity.badRequest().body("❌ Invalid cron expression: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while updating cron for task '{}'", request.getTaskName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Unexpected error while updating cron: " + e.getMessage());
        }
    }

    /**
     * Stop a scheduled task dynamically
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/stop")
    public ResponseEntity<String> stopTask(@RequestParam String taskName) {
        try {
            logger.info("Stopping task '{}'", taskName);
            dynamicSchedulerService.stopTask(taskName);
            return ResponseEntity.ok("🛑 Task '" + taskName + "' stopped successfully.");
        } catch (Exception e) {
            logger.error("Error while stopping task '{}'", taskName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error while stopping task: " + e.getMessage());
        }
    }

    /**
     * Check if a dynamic task is running
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/status")
    public ResponseEntity<String> checkStatus(@RequestParam String taskName) {
        try {
            boolean isRunning = dynamicSchedulerService.isTaskRunning(taskName);
            logger.info("Status check for task '{}': {}", taskName, isRunning ? "running" : "not running");
            return ResponseEntity.ok("📋 Task '" + taskName + "' is " + (isRunning ? "running." : "not running."));
        } catch (Exception e) {
            logger.error("Error while checking status for task '{}'", taskName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error while checking status: " + e.getMessage());
        }
    }

    @GetMapping("/all-crons")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> getAllCrons() {
        return ResponseEntity.ok(dynamicSchedulerService.getAllCronExpressions());
    }



}
