package com.example.engineer.View.Elements.languageBox;

import com.example.engineer.View.Elements.LanguageManager;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

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

        URL iconURL = getClass().getResource("/icons/flags/"+flag);
        if(iconURL!=null){
            ImageIcon imageIcon = new ImageIcon(iconURL);
            Image scaledIcon = imageIcon.getImage().getScaledInstance(32,32,Image.SCALE_SMOOTH);
            this.flag = new ImageIcon(scaledIcon);
        }
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
