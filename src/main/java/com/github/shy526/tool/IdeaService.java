package com.github.shy526.tool;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;

/**
 * 简化获取IdeaService
 *
 * @author shy526
 */
public class IdeaService {
    public static <T> T getInstance(@NotNull Project project, @NotNull Class<T> serviceClass) {
        return project.getService(serviceClass);
    }

    public static <T> T getInstance(@NotNull Class<T> serviceClass) {
        return ApplicationManager.getApplication().getService(serviceClass);
    }

    /**
     * 后台任务进度条管理
     *
     * @return ProgressManager
     */
    public static ProgressManager getProgressManager() {
        return ProgressManager.getInstance();
    }

    public static MessageBus getMessageBus() {
        return ApplicationManager.getApplication().getMessageBus();
    }

    public static PropertiesComponent getPropertiesComponent() {
        return PropertiesComponent.getInstance();
    }
}
