package com.github.shy526.topic.listener;

import com.github.shy526.obj.Chapter;
import com.github.shy526.topic.DownloadChapterTopic;

import java.util.List;

/**
 * 下载文件
 *
 * @author shy526
 */
public class DownloadChapterListener implements DownloadChapterTopic {
    @Override
    public List<String> downloadChapter(Chapter chapter) {
        return null;
    }
}
