package com.github.shy526.topic.listener;

import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.github.shy526.service.CartoonService;
import com.github.shy526.service.StorageService;
import com.github.shy526.tool.Common;
import com.github.shy526.tool.IdeaService;
import com.github.shy526.tool.NotificationSend;
import com.github.shy526.topic.CartoonPageTopic;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 下载文件
 *
 * @author shy526
 */
public class CartoonPageListener implements CartoonPageTopic {
    private static final StorageService STORAGE_SERVICE = IdeaService.getInstance(StorageService.class);
    private static final CartoonService CARTOON_SERVICE = IdeaService.getInstance(CartoonService.class);
    private static final Comparator<File> CARTOON_IMAG_COMPARATOR = (o1, o2) -> {
        Integer int1 = Integer.valueOf(o1.getName().substring(0, o1.getName().lastIndexOf(".")));
        Integer int2 = Integer.valueOf(o2.getName().substring(0, o2.getName().lastIndexOf(".")));
        return int1.compareTo(int2);
    };

    @Override
    public void page(int step) {
        String cacheDir = STORAGE_SERVICE.getCacheDir();
        Cartoon cartoon = STORAGE_SERVICE.getCartoon();
        Chapter chapter = STORAGE_SERVICE.getChapter();
        if (StringUtils.isEmpty(cacheDir)) {
            NotificationSend.info("not settings", 1000);
            return;
        }
        File fileDer = new File(cacheDir);
        if (!fileDer.exists() || fileDer.isFile()) {
            NotificationSend.info("not settings", 1000);
            return;
        }
        if (cartoon == null || chapter == null) {
            NotificationSend.info("not settings", 1000);
            return;
        }
        if (chapterPage(step)){
            return;
        }
        ProgressManager progressManager = IdeaService.getProgressManager();
        progressManager.run(new Task.Backgroundable(null, "-") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                CARTOON_SERVICE.selectImag(() -> {
                    File chapterDir = Common.nowChapterDir();
                    List<File> image = Arrays.stream(Objects.requireNonNull(chapterDir.listFiles(File::isFile))).sorted(CARTOON_IMAG_COMPARATOR).collect(Collectors.toList());
                    PropertiesComponent prop = PropertiesComponent.getInstance();
                    prop.setValue(IdeBackgroundUtil.EDITOR_PROP, image.get(STORAGE_SERVICE.getPage()).getAbsolutePath() + ",11,plain,center");
                }, indicator);
            }
        });

    }
    private boolean chapterPage(int step) {
        boolean result = true;
        Cartoon cartoon = STORAGE_SERVICE.getCartoon();
        Chapter chapter = STORAGE_SERVICE.getChapter();
        int page = STORAGE_SERVICE.getPage();
        page += step;
        boolean flag = page < 0;
        if (flag || page >= chapter.getTotal()) {
            Integer index = chapter.getIndex();
            List<Chapter> chapters = cartoon.getChapters();
            index += step;
            if (index < 0) {
                NotificationSend.info("not chapter zero", 1000);
                return result;
            }
            if (index >= chapters.size()) {
                //当漫画章节索引大于现在保存的章节时,更新漫画章节
                cartoon = CARTOON_SERVICE.selectChapter(cartoon);
                //更新设置
                STORAGE_SERVICE.setCartoon(cartoon);
                STORAGE_SERVICE.loadState(STORAGE_SERVICE);
            }
            chapters = cartoon.getChapters();
            if (index >= chapters.size()) {
                NotificationSend.info("not chapter zero", 1000);
                return result;
            }
            //获取新章节
            chapter = chapters.get(index);
            //更新设置
            STORAGE_SERVICE.setChapter(chapter);
            STORAGE_SERVICE.loadState(STORAGE_SERVICE);
            if (flag) {
                page = chapter.getTotal() - 1;
            } else {
                page = 0;
            }
        }
        STORAGE_SERVICE.setPage(page);
        STORAGE_SERVICE.loadState(STORAGE_SERVICE);
        result = false;
        return result;
    }

}



