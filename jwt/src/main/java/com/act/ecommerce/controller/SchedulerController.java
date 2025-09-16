package com.act.ecommerce.controller;

import com.act.ecommerce.entity.CronUpdateRequest;
import com.act.ecommerce.schedulers.DynamicSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    @Autowired
    private DynamicSchedulerService dynamicSchedulerService;

    /**
     * Dynamically update cron expression for any task
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/update-cron")
    public ResponseEntity<String> updateCron(@RequestBody CronUpdateRequest request) {
        dynamicSchedulerService.updateCronExpression(request.getTaskName(), request.getCronExpression());
        return ResponseEntity.ok("Cron expression for task '" + request.getTaskName() + "' updated successfully!");
    }

    /**
     * Stop a scheduled task dynamically
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/stop")
    public ResponseEntity<String> stopTask(@RequestParam String taskName) {
        dynamicSchedulerService.stopTask(taskName);
        return ResponseEntity.ok("Task '" + taskName + "' stopped successfully.");
    }

    /**
     * Check if a dynamic task is running
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/status")
    public ResponseEntity<String> checkStatus(@RequestParam String taskName) {
        boolean isRunning = dynamicSchedulerService.isTaskRunning(taskName);
        return ResponseEntity.ok("Task '" + taskName + "' is " + (isRunning ? "running." : "not running."));
    }
}
