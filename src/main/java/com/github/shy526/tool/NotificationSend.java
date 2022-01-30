package com.github.shy526.tool;

import com.intellij.notification.*;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Notification 通知
 *
 * @author shy526
 */
public class NotificationSend {
    private static final NotificationGroup NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup(PluginBundle.message("plugin.notification.group.id"));


    public static void error(String content) {
        send(content, null, NotificationType.ERROR);
    }

    public static void info(String content) {
        send(content, null, NotificationType.INFORMATION);
    }

    public static void warn(String content) {
        send(content, null, NotificationType.WARNING);
    }

    public static void error(String content, Integer sleep) {
        send(content, sleep, NotificationType.ERROR);
    }

    public static void info(String content, Integer sleep) {
        send(content, sleep, NotificationType.INFORMATION);
    }

    public static void warn(String content, Integer sleep) {
        send(content, sleep, NotificationType.WARNING);
    }

    public static void send(String content, Integer sleep, NotificationType type) {
        Notification notification = NOTIFICATION_GROUP.createNotification(content, type);
        Notifications.Bus.notify(notification);
        if (sleep != null) {
            sleep(sleep, notification::expire);
        }

    }


    private static void sleep(Integer sleep, Runnable runnable) {
        ProgressManager progressManager = IdeaService.getProgressManager();
        progressManager.run(new Task.ConditionalModal(null, "NotificationSend sleep", true, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
            @Override
            public void run(ProgressIndicator indicator) {
                try {
                    indicator.setIndeterminate(false);
                    indicator.setText("NotificationSend sleep start");
                    indicator.setFraction(0);
                    TimeUnit.MILLISECONDS.sleep(sleep);
                    indicator.setFraction(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    runnable.run();
                }
            }
        });
    }
}
