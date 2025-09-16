package com.act.ecommerce.schedulers;

import com.act.ecommerce.dao.ScheduleConfigRepository;
import com.act.ecommerce.entity.ScheduleConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class DynamicSchedulerService {

    @Autowired
    private ScheduleConfigRepository scheduleConfigRepository;

    @Autowired
    private OrderScheduler orderScheduler;

    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public DynamicSchedulerService() {
        taskScheduler.setPoolSize(5); // Adjust based on expected concurrency
        taskScheduler.setThreadNamePrefix("DynamicScheduler-");
        taskScheduler.initialize();
    }

    @PostConstruct
    public void initializeAllTasks() {
        scheduleConfigRepository.findAll().forEach(config -> {
            restartScheduledTask(config.getTaskName(), config.getCronExpression());
        });
    }

    public void updateCronExpression(String taskName, String newCron) {
        ScheduleConfig config = scheduleConfigRepository.findByTaskName(taskName)
                .orElse(new ScheduleConfig());
        config.setTaskName(taskName);
        config.setCronExpression(newCron);
        scheduleConfigRepository.save(config);

        restartScheduledTask(taskName, newCron);
    }

    private void restartScheduledTask(String taskName, String cronExpression) {
        ScheduledFuture<?> existingTask = scheduledTasks.get(taskName);
        if (existingTask != null) {
            existingTask.cancel(false);
        }

        Runnable taskRunnable = getRunnableForTask(taskName);
        if (taskRunnable == null) {
            throw new IllegalArgumentException("No runnable found for task: " + taskName);
        }

        ScheduledFuture<?> newTask = taskScheduler.schedule(taskRunnable, new CronTrigger(cronExpression));
        scheduledTasks.put(taskName, newTask);
    }

    private Runnable getRunnableForTask(String taskName) {
        switch (taskName) {
            case "order-shipping":
                return orderScheduler::processPlacedOrdersForShipping;
            case "order-delivery":
                return orderScheduler::processShippedOrdersForDelivery;
//            case "email-reminder":
//                return orderScheduler::sendPendingEmailReminders;
            // Add more cases as needed
            default:
                return null;
        }
    }

    public void stopTask(String taskName) {
        ScheduledFuture<?> task = scheduledTasks.remove(taskName);
        if (task != null) {
            task.cancel(false);
        }
    }

    public boolean isTaskRunning(String taskName) {
        ScheduledFuture<?> task = scheduledTasks.get(taskName);
        return task != null && !task.isCancelled();
    }
}
