package com.github.shy526.service;

import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author shy526
 */
@State(name = "com.github.shy526.service.StorageService", storages = {@Storage("StorageService.xml")})
@Data
public class StorageService implements PersistentStateComponent<StorageService> {
    private Cartoon cartoon;
    private Chapter chapter;
    private Integer page;
    private String cacheDir;

    public static StorageService getInstance() {
        return ApplicationManager.getApplication().getService(StorageService.class);

    }

    @Override
    public @Nullable StorageService getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull StorageService state) {
        XmlSerializerUtil.copyBean(state, this);
    }

}
