package com.github.shy526.action;

import com.github.shy526.tool.IdeaService;
import com.github.shy526.topic.CartoonPageTopic;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * alt+.
 * @author shy526
 */
public class CartoonPageRight extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        CartoonPageTopic downloadChapterTopic = IdeaService.getMessageBus().syncPublisher(CartoonPageTopic.CARTOON_PAGE_TOPIC);
        downloadChapterTopic.page(1);
    }
}
