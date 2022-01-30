package com.github.shy526.action;

import com.github.shy526.factory.CartoonServiceFactory;
import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.github.shy526.service.CartoonService;
import com.github.shy526.service.StorageService;
import com.github.shy526.tool.IdeaService;
import com.github.shy526.tool.NotificationSend;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 处理翻页逻辑
 *
 * @author shy526
 */
public interface CartoonPage {
    static boolean FLAG = false;

    /**
     * 翻页
     *
     * @param step 步长
     */
    default void page(int step, AnActionEvent anActionEvent) {
        StorageService storageService = IdeaService.getInstance(StorageService.class);
        CartoonService cartoonService = CartoonServiceFactory.getInstance();
        String cacheDir = storageService.getCacheDir();
        Cartoon cartoon = storageService.getCartoon();
        Chapter chapter = storageService.getChapter();
        Boolean flag = storageService.getFlag();
        if (flag) {
            NotificationSend.info("load ing", 1000);
            return;
        }
        int page = storageService.getPage();
        page += step;
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
        Chapter nextChapter = getNextChapter(step);
        boolean cacheFlag = true;
        if (page > chapter.getTotal() || page < 1) {
            //页码翻动章节
            List<Chapter> chapters = cartoon.getChapters();


            if (nextChapter == null) {
                NotificationSend.info("not chapter", 1000);
                return;
            }
            chapter = nextChapter;
            storageService.setChapter(chapter);
            storageService.loadState(storageService);
            page = page < 1 ? chapter.getTotal() : 1;
            cacheFlag = false;
        }
        if (nextChapter != null && cacheFlag) {
            chapterCache(nextChapter);
        }
        File chapterDir = storageService.nowChapterDir();
        if (!chapterDir.exists()) {
            chapterDir.mkdirs();
        }
        int finalPage = page;
        Consumer<Chapter> consumer = chapter1 -> {
            List<File> image = Arrays.stream(Objects.requireNonNull(chapterDir.listFiles(File::isFile))).sorted(Comparator.comparing(o -> Integer.valueOf(o.getName().substring(0, o.getName().lastIndexOf("."))))).collect(Collectors.toList());
            PropertiesComponent prop = PropertiesComponent.getInstance();
            int i = finalPage - 1;
            if (i >= image.size()) {
                NotificationSend.error("error imag");
            }
            prop.setValue(IdeBackgroundUtil.EDITOR_PROP, image.get(i).getAbsolutePath() + ",11,plain,center");
            storageService.setFlag(false);
            storageService.setPage(finalPage);
            storageService.loadState(storageService);
            IdeBackgroundUtil.repaintAllWindows();
        };
        File[] files = chapterDir.listFiles(File::isFile);
        if (files == null || chapter.getTotal() != files.length) {
            storageService.setFlag(true);
            cartoonService.selectImag(chapter, chapterDir.toPath(), consumer);

        } else {
            consumer.accept(chapter);
        }


    }

    private void chapterCache(Chapter chapter) {
        StorageService storageService = IdeaService.getInstance(StorageService.class);
        CartoonService cartoonService = CartoonServiceFactory.getInstance();
        String cacheDir = storageService.getCacheDir();
        Cartoon cartoon = storageService.getCartoon();
        File chapterDir = Path.of(cacheDir).resolve(cartoon.toString()).resolve(chapter.toString()).toFile();
        if (chapterDir.exists()) {
            return;
        }
        chapterDir.mkdirs();
        File[] files = chapterDir.listFiles(File::isFile);
        if (files == null || chapter.getTotal() != files.length) {
            storageService.setFlag(true);
            cartoonService.selectImag(chapter, chapterDir.toPath(), null);

        }
    }

    private Chapter getNextChapter(int step) {
        StorageService storageService = IdeaService.getInstance(StorageService.class);
        CartoonService cartoonService = CartoonServiceFactory.getInstance();
        Cartoon cartoon = storageService.getCartoon();
        List<Chapter> chapters = cartoon.getChapters();
        Chapter chapter = storageService.getChapter();
        int next = -1;
        int chapterTotal = chapters.size();
        for (int i = 0; i < chapterTotal; i++) {
            if (chapter.equals(chapters.get(i))) {
                next = i + step;
                break;
            }
        }
        if (next >= chapterTotal) {
            //拉取新章节
            chapters = cartoonService.selectChapter(cartoon);
            cartoon.setChapters(chapters);
            storageService.loadState(storageService);
        }
        if (next < chapterTotal && next > 0) {
            return chapters.get(next);
        }
        return null;
    }

}
