package com.github.shy526.tool;

import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.github.shy526.service.StorageService;

import java.io.File;
import java.nio.file.Path;

public class Common {
    private static final StorageService STORAGE_SERVICE = IdeaService.getInstance(StorageService.class);
    public static File nowChapterDir() {
        String cacheDir = STORAGE_SERVICE.getCacheDir();
        Cartoon cartoon = STORAGE_SERVICE.getCartoon();
        Chapter chapter = STORAGE_SERVICE.getChapter();
        if (cacheDir == null || cartoon == null || chapter == null) {
            return null;
        }
        return Path.of(cacheDir).resolve(cartoon.toString()).resolve(chapter.toString()).toFile();
    }
}
