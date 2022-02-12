package com.github.shy526.service;

import com.github.shy526.factory.ObjFactory;
import com.github.shy526.http.HttpClientService;
import com.github.shy526.http.HttpResult;
import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.github.shy526.tool.Common;
import com.github.shy526.tool.IdeaService;
import com.github.shy526.tool.NotificationSend;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author shy526
 */
public class CartoonService {
    private final static HttpClientService HTTP_CLIENT_SERVICE;
    private final static String MAIN = "https://xmanhua.com/";
    private final static String SELECT_URL = MAIN + "search?title=%s&page=%s";
    private final static String CHAPTER_URL = MAIN + "/%s/";
    private final static String IMAG_URL = MAIN + "/%s/chapterimage.ashx?cid=%s&page=%s&key=&_cid=%s&_mid=%s";
    private final static Map<String, String> HEADER = new HashMap<>(1);
    private final static ScriptEngine JS_SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("javascript");
    private static final StorageService STORAGE_SERVICE = IdeaService.getInstance(StorageService.class);
    private static final Logger LOG = Logger.getInstance(CartoonService.class);

    static {
        HEADER.put("referer", MAIN);
        HTTP_CLIENT_SERVICE = IdeaService.getInstance(HttpClientService.class);
    }

    /**
     * 查询所有漫画
     *
     * @param sTitle 漫画标题
     * @return List<Cartoon>
     */
    public List<Cartoon> selectCartoon(String sTitle) {
        if (sTitle == null || "".equals(sTitle.trim())) {
            return new ArrayList<>();
        }
        try {
            sTitle = URLEncoder.encode(sTitle, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        String url = String.format(SELECT_URL, sTitle, 0);
        try (HttpResult mPage = HTTP_CLIENT_SERVICE.get(url, null, HEADER)) {
            HttpResult httpResult = HTTP_CLIENT_SERVICE.get(url, null, HEADER);
            String entityStr = httpResult.getEntityStr();
            Document document = Jsoup.parse(entityStr);
            return ObjFactory.produceCartoon(document);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * 查询该漫画的所有章节
     *
     * @param cartoon Cartoon
     * @return Cartoon
     */
    public Cartoon selectChapter(Cartoon cartoon) {
        String url = String.format(CHAPTER_URL, cartoon.getId());
        try (HttpResult mPage = HTTP_CLIENT_SERVICE.get(url, null, HEADER)) {
            Document doc = Jsoup.parse(mPage.getEntityStr());
            List<Chapter> chapters = ObjFactory.produceChapter(doc);
            cartoon.setChapters(chapters);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return cartoon;
    }

    public void selectImag(Runnable runnable, ProgressIndicator indicator) {
        Chapter chapter = STORAGE_SERVICE.getChapter();
        Cartoon cartoon = STORAGE_SERVICE.getCartoon();

        Integer pageIndex = STORAGE_SERVICE.getPage();
        File file = Common.nowChapterDir();
        if (!file.exists()) {
            file.mkdirs();
        }
        List<File> collect = Arrays.stream(Objects.requireNonNull(file.listFiles(pathname -> {
            String name = pathname.getName();
            String substring = name.substring(0, name.lastIndexOf("."));
            return substring.equals(pageIndex + "");
        }))).collect(Collectors.toList());
        AtomicBoolean flag = new AtomicBoolean(true);
        if (!collect.isEmpty()) {
            flag.set(false);
            runnable.run();
            return;
        }
        Path path = file.toPath();

        int index = 1;
        int imagIndex = 0;
        for (int page = 1; page <= chapter.getTotal(); page += index) {
            String mId = chapter.getId();
            String id = chapter.getId().replaceAll("m", "");
            String jsUrl = String.format(IMAG_URL, mId, id, page, id, cartoon.getId().replaceAll("xm", ""));
            indicator.setText(chapter + "->" + jsUrl);
            if (indicator.isCanceled()) {
                break;
            }
            HttpResult httpResult = HTTP_CLIENT_SERVICE.get(jsUrl, null, HEADER);
            try {
                ScriptObjectMirror eval = (ScriptObjectMirror) JS_SCRIPT_ENGINE.eval(httpResult.getEntityStr());
                if (eval == null) {
                    break;
                }
                index = eval.size();
                for (Map.Entry entry : eval.entrySet()) {
                    Object v = entry.getValue();
                    File imagFile = path.resolve(imagIndex + ".jpg").toFile();
                    if (imagFile.exists()) {
                        if (imagIndex == pageIndex && flag.get()) {
                            flag.set(false);
                            runnable.run();
                        }
                        imagIndex++;
                        continue;
                    }
                    indicator.setText(chapter + "->" + v.toString());
                    if (indicator.isCanceled()) {
                        break;
                    }
                    downloadImag(v, imagFile);
                    if (imagIndex == pageIndex && flag.get()) {
                        flag.set(false);
                        runnable.run();
                    }
                    imagIndex++;
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (runnable != null && flag.get()) {
            runnable.run();
        }


    }

    private void downloadImag(Object v, File imagFile) {
        try (HttpResult down = HTTP_CLIENT_SERVICE.get(v.toString()); InputStream imagIn = down.getInputStream(); OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(imagFile))) {
            IOUtils.copy(imagIn, fileOut);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
