package com.github.shy526.service;

import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.github.shy526.tool.AbsStorageService;
import com.github.shy526.tool.IdeaService;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.annotations.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * @author shy526
 */
@EqualsAndHashCode(callSuper = true)
@State(name = "com.github.shy526.service.StorageService", storages = {@Storage("StorageService.xml")})
@Data
public class StorageService extends AbsStorageService<StorageService> {

    private Cartoon cartoon;
    private Chapter chapter;
    private Integer page;
    private String cacheDir;
    @Transient
    private Boolean flag = false;

    @Transient
    public Boolean getFlag() {
        return this.flag;
    }



    @Override
    public @Nullable StorageService getState() {
        return this;
    }

}
