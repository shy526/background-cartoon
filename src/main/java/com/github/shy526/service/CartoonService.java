package com.github.shy526.service;

import com.github.shy526.factory.ObjFactory;
import com.github.shy526.http.HttpClientFactory;
import com.github.shy526.http.HttpClientProperties;
import com.github.shy526.http.HttpClientService;
import com.github.shy526.http.HttpResult;
import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.intellij.openapi.diagnostic.Logger;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author shy526
 */
public class CartoonService {
    private final static HttpClientService HTTP_CLIENT_SERVICE;
    private final static String MAIN = "https://xmanhua.com/";
    private final static String SELECT_URL = MAIN + "search?title=%s&page=%s";
    private final static String CHAPTER_URL = MAIN + "/%s/";
    private final static String IMAG_URL = MAIN + "/%s/chapterimage.ashx?cid=%s&page=%s&key=";
    private final static ScriptEngine JS_SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("javascript");
    private static final Logger LOG = Logger.getInstance(CartoonService.class);
    private final static Pattern SUFFIX = Pattern.compile(".*\\.(jpg|png)?.*");

    static {
        HttpClientProperties properties = new HttpClientProperties();
        Map<String, String> header = properties.getHeader();
        header.put("referer", MAIN);
        HTTP_CLIENT_SERVICE = HttpClientFactory.getHttpClientService(properties);
    }

    public List<Cartoon> selectCartoon(String sTitle) {
        if (sTitle == null || "".equals(sTitle.trim())) {
            return null;
        }
        try {
            sTitle = URLEncoder.encode(sTitle, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String url = String.format(SELECT_URL, sTitle, 0);
        HttpResult httpResult = HTTP_CLIENT_SERVICE.get(url);
        String entityStr = httpResult.getEntityStr();
        Document document = Jsoup.parse(entityStr);
        return ObjFactory.produceCartoon(document);

    }


    public List<Chapter> selectChapter(Cartoon cartoon) {
        String url = String.format(CHAPTER_URL, cartoon.getId());
        HttpResult mPage = HTTP_CLIENT_SERVICE.get(url);
        Document doc = Jsoup.parse(mPage.getEntityStr());
        return ObjFactory.produceChapter(doc);
    }

    public List<String> selectImag(Chapter chapter, Path path) {
        int index = 1;
        List<String> result = new ArrayList<String>();
        byte[] buff = new byte[1024 * 4];
        int imagIndex = 0;
        for (int page = 1; page <= chapter.getTotal(); page += index) {
            String mId = chapter.getId();
            String id = chapter.getId().replaceAll("m", "");
            HttpResult httpResult = HTTP_CLIENT_SERVICE.get(String.format(IMAG_URL, mId, id, page));
            try {

                ScriptObjectMirror eval = (ScriptObjectMirror) JS_SCRIPT_ENGINE.eval(httpResult.getEntityStr());
                if (eval != null) {
                    index = eval.size();
                    for (Map.Entry entry : eval.entrySet()) {
                        Object v = entry.getValue();
                        Path imag = path.resolve(imagIndex + ".jpg");
                        try (HttpResult down = HTTP_CLIENT_SERVICE.get(v.toString());
                             InputStream liveIn = new BufferedInputStream(down.getResponse().getEntity().getContent());
                             BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(imag.toFile()))) {
                            int len = -1;
                            while ((len = liveIn.read(buff)) != -1) {
                                fileOut.write(buff, 0, len);
                            }
                            imagIndex++;
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }

                } else {
                    break;
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return result;
    }
}
