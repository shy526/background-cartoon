package com.github.shy526.ui;

import com.github.shy526.obj.Cartoon;
import com.github.shy526.obj.Chapter;
import com.github.shy526.service.CartoonService;
import com.github.shy526.factory.CartoonServiceFactory;
import com.github.shy526.service.StorageService;
import com.github.shy526.tool.IdeaService;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.FixedComboBoxEditor;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTextField;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

public class Settings implements Configurable {

    public static final String CACHE_DER_KEY = "cacheDer";
    public static final String CARTOON_KEY = "cartoon";
    public static final String CHAPTER_KEY = "chapter";
    public static final String PAGE_KEY = "page";
    private JPanel rootPanel;
    private JLabel searchLabel;
    private TextFieldWithBrowseButton cacheBrowse;
    private JLabel caCheLabel;
    private ComboBox<Cartoon> searchComboBox;
    private JLabel chapterLabel;
    private JLabel pageLabel;
    private ComboBox<Chapter> chapterComboBox;
    private JSpinner pageSpinner;
    private final CartoonService cartoonService = CartoonServiceFactory.getInstance();

    @Override
    public @Nullable JComponent createComponent() {
        chapterPageVisible(false);
        FixedComboBoxEditor comboBoxEditor = new FixedComboBoxEditor();
        comboBoxEditor.getField().addActionListener(e -> {
            selectCartoonAction(comboBoxEditor.getField());
            if (searchComboBox.getItemCount() > 0) {
                searchComboBox.showPopup();
            }
        });
        searchComboBox.setEditor(comboBoxEditor);
        searchComboBox.setEditable(true);
        searchComboBox.addItemListener(e -> {
            Cartoon cartoon = (Cartoon) e.getItem();
            List<Chapter> chapters = cartoonService.selectChapter(cartoon);
            cartoon.setChapters(chapters);
            chapterComboBox.removeAllItems();
            chapters.forEach(item -> chapterComboBox.addItem(item));
            chapterPageVisible(true);
        });
        chapterComboBox.addItemListener(e -> {
            Chapter item = (Chapter) e.getItem();
            SpinnerNumberModel model = (SpinnerNumberModel) pageSpinner.getModel();
            model.setMinimum(1);
            model.setMaximum(item.getTotal());
            model.setValue(1);
        });
        cacheBrowse.addBrowseFolderListener(new TextBrowseFolderListener(FileChooserDescriptorFactory.createSingleFolderDescriptor()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String current = cacheBrowse.getText();
                VirtualFile toSelect = null;
                if (!current.isEmpty()) {
                    toSelect = LocalFileSystem.getInstance().findFileByIoFile(new File(current));
                }
                VirtualFile[] virtualFiles = FileChooser.chooseFiles(myFileChooserDescriptor, getProject(), toSelect);
                if (virtualFiles.length > 0) {
                    VirtualFile virtualFile = virtualFiles[0];
                    cacheBrowse.setText(virtualFile.getPath());
                }
            }
        });

        return rootPanel;
    }

    private void selectCartoonAction(JBTextField field) {
        field.setEditable(false);
        searchComboBox.hidePopup();
        String title = field.getText();
        if (StringUtils.isEmpty(title)) {
            chapterPageVisible(false);
        } else {
            List<Cartoon> cartoons = cartoonService.selectCartoon(title);
            searchComboBox.removeAllItems();
            cartoons.forEach(item -> searchComboBox.addItem(item));
        }
        field.setEditable(true);
        field.setFocusable(true);
        field.setText(title);
    }

    @Override
    public boolean isModified() {
        StorageService storageService = IdeaService.getInstance(StorageService.class);
        String oldCacheDir = storageService.getCacheDir();
        Cartoon oldCartoon = storageService.getCartoon();
        Chapter oldChapter = storageService.getChapter();
        Integer oldPage = storageService.getPage();
        String newCacheDir = cacheBrowse.getText();
        Cartoon newCartoon = searchComboBox.getItem();
        Chapter newChapter = chapterComboBox.getItem();
        Integer newPage = ((Integer) pageSpinner.getValue());
        return modifiedValue(oldCacheDir, newCacheDir) || modifiedValue(oldCartoon, newCartoon) || modifiedValue(oldChapter, newChapter) || modifiedValue(oldPage, newPage);
    }

    private boolean modifiedValue(String oldValue, String newValue) {
        if (StringUtils.isEmpty(oldValue) && StringUtils.isEmpty(newValue)) {
            return false;
        }
        if (StringUtils.isEmpty(oldValue) || StringUtils.isEmpty(newValue)) {
            return true;
        }
        return !oldValue.equals(newValue);
    }

    private boolean modifiedValue(Object oldValue, Object newValue) {
        if (oldValue == null && newValue == null) {
            return false;
        }
        if (oldValue == null || newValue == null) {
            return true;
        }
        return !oldValue.equals(newValue);
    }

    @Override
    public void apply() throws ConfigurationException {
        StorageService storageService = IdeaService.getInstance(StorageService.class);
        storageService.setCartoon(searchComboBox.getItem());
        storageService.setCacheDir(cacheBrowse.getText());
        storageService.setChapter(chapterComboBox.getItem());
        storageService.setPage((Integer) pageSpinner.getValue());
        storageService.loadState(storageService);
    }

    @Override
    public void reset() {
        StorageService storageService = IdeaService.getInstance(StorageService.class);
        String oldCacheDir = storageService.getCacheDir();
        Cartoon oldCartoon = storageService.getCartoon();
        Chapter oldChapter = storageService.getChapter();
        Integer oldPage = storageService.getPage();
        cacheBrowse.setText(oldCacheDir);
        boolean flag = oldCartoon != null;
        if (flag) {
            searchComboBox.addItem(oldCartoon);
            chapterComboBox.setSelectedItem(oldChapter);
        }
        if (oldPage != null) {
            pageSpinner.setValue(oldPage);
        }
        chapterPageVisible(flag);


    }


    @Nls
    @Override
    public String getDisplayName() {
        return "Background Cartoon";
    }

    private void chapterPageVisible(boolean flag) {
        pageLabel.setVisible(flag);
        pageSpinner.setVisible(flag);
        chapterLabel.setVisible(flag);
        chapterComboBox.setVisible(flag);
    }
}
