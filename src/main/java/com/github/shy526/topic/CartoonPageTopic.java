package com.github.shy526.topic;

import com.github.shy526.obj.Chapter;
import com.intellij.util.messages.Topic;

/**
 * 下载漫画章节
 *
 * @author shy526
 */
public interface CartoonPageTopic {
    Topic<CartoonPageTopic> CARTOON_PAGE_TOPIC = Topic.create(CartoonPageTopic.class.getName(), CartoonPageTopic.class);
    void page(int step);
}
