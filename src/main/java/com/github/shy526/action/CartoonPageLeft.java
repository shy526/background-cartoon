package com.github.shy526.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * alet+,
 * @author shy526
 */
public class CartoonPageLeft extends AnAction implements CartoonPage {

    @Override
    public void actionPerformed(AnActionEvent e) {
        page(-1);
    }
}