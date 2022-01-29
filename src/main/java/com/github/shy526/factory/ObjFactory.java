package com.github.shy526.factory;

import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shy526
 */
public class ObjFactory {
    private static final Pattern X_ID_PATTERN = Pattern.compile("（(\\d+)P）");

    public static List<Cartoon> produceCartoon(Document document) {
        List<Cartoon> cartoons = new ArrayList<>();
        Elements mhItems = document.select("div.mh-item");
        for (Element item : mhItems) {
            String cover = item.select("img.mh-cover").attr("src");
            String id = item.select("div.mh-item-detali>h2>a").attr("href").replaceAll("/", "");
            String title = item.select("div.mh-item-detali>h2>a").attr("title");
            Cartoon cartoon = new Cartoon();
            cartoon.setCover(cover);
            cartoon.setTitle(title);
            cartoon.setId(id);
            cartoons.add(cartoon);
        }
        return cartoons;
    }

    public static List<Chapter> produceChapter(Document document) {
        final Elements select = document.select("#chapterlistload>a");
        List<Chapter> chapters = new ArrayList<>();
        for (int i = select.size() - 1; i >= 0; i--) {
            Element element = select.get(i);
            Chapter chapter = new Chapter();
            String id = element.attr("href").replaceAll("/", "");
            chapter.setId(id);
            Matcher totalMatcher = X_ID_PATTERN.matcher(element.select("span").text());
            if (totalMatcher.find()) {
                chapter.setTotal(Integer.parseInt(totalMatcher.group(1)));
            }
            chapter.setTitle(element.ownText());
            chapters.add(chapter);
        }
        return chapters;
    }
}
