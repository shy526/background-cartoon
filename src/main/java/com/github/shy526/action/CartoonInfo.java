package com.github.shy526.action;

import com.github.shy526.factory.CartoonServiceFactory;
import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.github.shy526.service.CartoonService;
import com.github.shy526.service.StorageService;
import com.github.shy526.tool.NotificationSend;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CartoonInfo extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        StorageService storageService = StorageService.getInstance();
        Cartoon cartoon = storageService.getCartoon();
        Chapter chapter = storageService.getChapter();
        Integer page = storageService.getPage();
        NotificationSend.info("now:-->" + cartoon + "---" + chapter + "---" +page,1000);
    }
}
