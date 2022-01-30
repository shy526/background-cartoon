package com.github.shy526.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * alt+.
 * @author shy526
 */
public class CartoonPageRight extends AnAction implements CartoonPage {


    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        page(1,anActionEvent);
    }
}
