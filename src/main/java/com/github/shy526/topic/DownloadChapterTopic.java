package com.github.shy526.topic;

import com.github.shy526.obj.Chapter;
import com.intellij.util.messages.Topic;

import java.util.List;

/**
 * 下载漫画章节
 *
 * @author shy526
 */
public interface DownloadChapterTopic {
    Topic<DownloadChapterTopic> DOWNLOAD_CHAPTER_TOPIC = Topic.create(DownloadChapterTopic.class.getName(), DownloadChapterTopic.class);

    /**
     * 下载文件
     *
     * @param chapter chapter
     * @return List<String>
     */
    List<String> downloadChapter(Chapter chapter);
}
