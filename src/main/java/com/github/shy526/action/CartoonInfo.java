package com.github.shy526.action;

import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.github.shy526.service.StorageService;
import com.github.shy526.tool.IdeaService;
import com.github.shy526.tool.NotificationSend;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.impl.IdeBackgroundUtil;

public class CartoonInfo extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        StorageService storageService = IdeaService.getInstance(StorageService.class);
        Cartoon cartoon = storageService.getCartoon();
        Chapter chapter = storageService.getChapter();
        Integer page = storageService.getPage();
        PropertiesComponent prop = PropertiesComponent.getInstance();
        //文件名,不透明度
        System.out.println( prop.getValue(IdeBackgroundUtil.EDITOR_PROP));
        boolean flag=false;
        String str=prop.getValue(IdeBackgroundUtil.EDITOR_PROP);
        if (flag){
            prop.setValue(IdeBackgroundUtil.EDITOR_PROP, str);
            IdeBackgroundUtil.repaintAllWindows();
        }
        NotificationSend.info("now--->" + cartoon + "--->" + chapter + "--->" + page, 1000);
    }
}
