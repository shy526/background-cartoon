package com.github.shy526.action;

import com.github.shy526.factory.CartoonServiceFactory;
import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.github.shy526.service.CartoonService;
import com.github.shy526.service.StorageService;
import com.github.shy526.tool.NotificationSend;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 处理翻页逻辑
 * @author shy526
 */
public interface CartoonPage {
    /**
     * 翻页
     *
     * @param step 步长
     */
    default void page(int step) {
        StorageService storageService = StorageService.getInstance();
        CartoonService cartoonService = CartoonServiceFactory.getInstance();
        String cacheDir = storageService.getCacheDir();
        Cartoon cartoon = storageService.getCartoon();
        Chapter chapter = storageService.getChapter();
        Integer page = storageService.getPage();
        page += step;
        if (StringUtils.isEmpty(cacheDir)) {
            return;
        }
        File fileDer = new File(cacheDir);
        if (!fileDer.exists() || fileDer.isFile()) {
            return;
        }
        if (cartoon == null || chapter == null) {
            return;
        }
        if (page >= chapter.getTotal() || page < 0) {
            //页码翻动章节
            List<Chapter> chapters = cartoon.getChapters();
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
            if (next > chapters.size() || next < 0) {
                //没有跟多章节
                return;
            }
            chapter = chapters.get(next);
            storageService.setChapter(chapter);
            storageService.loadState(storageService);
            page = page < 0 ? chapter.getTotal() - 1 : 0;
        }
        File chapterDir = Path.of(cacheDir).resolve(cartoon.toString()).resolve(chapter.toString()).toFile();

        if (!chapterDir.exists()) {
            chapterDir.mkdirs();
        }
        File[] files = chapterDir.listFiles(File::isFile);
        if (files == null || chapter.getTotal() != files.length) {
            NotificationSend.info("load-->" + chapter + "--->start");
            List<String> strings = cartoonService.selectImag(chapter, chapterDir.toPath());
            NotificationSend.info("load-->" + chapter + "--->end");
        }
        List<File> image = Arrays.stream(Objects.requireNonNull(chapterDir.listFiles(File::isFile))).sorted(Comparator.comparing(o -> Integer.valueOf(o.getName().substring(0, o.getName().lastIndexOf("."))))).collect(Collectors.toList());
        PropertiesComponent prop = PropertiesComponent.getInstance();
        prop.setValue(IdeBackgroundUtil.FRAME_PROP, null);
        prop.setValue(IdeBackgroundUtil.EDITOR_PROP, image.get(page).getAbsolutePath());
        NotificationSend.info("go-->" + cartoon + "--->" + chapter + "--->" + page);
        storageService.setPage(page);
        storageService.loadState(storageService);
        IdeBackgroundUtil.repaintAllWindows();
    }
}
