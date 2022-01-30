package com.github.shy526.service;

import com.github.shy526.factory.ObjFactory;
import com.github.shy526.http.HttpClientFactory;
import com.github.shy526.http.HttpClientProperties;
import com.github.shy526.http.HttpClientService;
import com.github.shy526.http.HttpResult;
import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.github.shy526.tool.IdeaService;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author shy526
 */
public class CartoonService {
    private final static HttpClientService HTTP_CLIENT_SERVICE;
    private final static String MAIN = "https://xmanhua.com/";
    private final static String SELECT_URL = MAIN + "search?title=%s&page=%s";
    private final static String CHAPTER_URL = MAIN + "/%s/";
    private final static String IMAG_URL = MAIN + "/%s/chapterimage.ashx?cid=%s&page=%s&key=";
    private final static Map<String, String> HEADER = new HashMap<>(1);
    private final static ScriptEngine JS_SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("javascript");
    private static final Logger LOG = Logger.getInstance(CartoonService.class);

    static {
        HEADER.put("referer", MAIN);
        HTTP_CLIENT_SERVICE = IdeaService.getInstance(HttpClientService.class);
    }

    public List<Cartoon> selectCartoon(String sTitle) {
        if (sTitle == null || "".equals(sTitle.trim())) {
            return new ArrayList<>();
        }
        try {
            sTitle = URLEncoder.encode(sTitle, "utf-8");
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


    public List<Chapter> selectChapter(Cartoon cartoon) {
        String url = String.format(CHAPTER_URL, cartoon.getId());
        try (HttpResult mPage = HTTP_CLIENT_SERVICE.get(url, null, HEADER)) {

            Document doc = Jsoup.parse(mPage.getEntityStr());
            return ObjFactory.produceChapter(doc);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return new ArrayList<>();
    }

    public void selectImag(Chapter chapter, Path path, Consumer<Chapter> consumer) {
        ProgressManager progressManager = IdeaService.getProgressManager();
        progressManager.run(new Task.Backgroundable(null, "SelectImag->" + chapter) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                int index = 1;
                List<String> result = new ArrayList<String>();
                int imagIndex = 0;
                for (int page = 1; page <= chapter.getTotal(); page += index) {
                    String mId = chapter.getId();
                    String id = chapter.getId().replaceAll("m", "");
                    String jsUrl = String.format(IMAG_URL, mId, id, page);
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
                            imagIndex++;
                            File imagFile = path.resolve(imagIndex + ".jpg").toFile();
                            if (imagFile.exists()) {
                                continue;
                            }
                            indicator.setText(chapter + "->" + v.toString());
                            if (indicator.isCanceled()) {
                                break;
                            }
                            try (HttpResult down = HTTP_CLIENT_SERVICE.get(v.toString()); InputStream imagIn = down.getInputStream(); OutputStream fileOut = new BufferedOutputStream(new FileOutputStream(imagFile))) {
                                IOUtils.copy(imagIn, fileOut);
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                if (consumer != null) {
                    consumer.accept(chapter);
                }

            }
        });

    }
}
