package com.example.engineer.View.Elements.languageBox;

import com.example.engineer.View.Elements.IconLoader;
import com.example.engineer.View.Elements.Language.LanguageManager;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

public class LanguageItem {
    @Getter
    private String code;
    @Getter
    @Setter
    private String language;
    @Getter
    private ImageIcon flag;

    public LanguageItem(String language,String code, String flag) {
        this.language = language;
        this.code = code;
        this.flag = IconLoader.getFlagIcon(flag);
    }

    public void setLanguage(){
        setLanguage(LanguageManager.getLanguageName(code));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LanguageItem)
            return code.equals(((LanguageItem) obj).code);
        return false;
    }
}
