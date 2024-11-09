package com.example.engineer.View.Elements.Language;

import com.example.engineer.View.Elements.FXElementsProviders.FXIconLoader;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

public class LanguageEntry {
    @Getter
    private final String code;
    @Getter
    private final Image flagIcon;
    @Getter
    @Setter
    private String name;

    public LanguageEntry(String code, String name) {
        this.code = code;
        this.name = name;
        this.flagIcon = FXIconLoader.getFlagIcon(code+".png");
    }

    public void setLanguage(){
        setName(LanguageManager.getLanguageName(code));
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof LanguageEntry)
            return code.equals(((LanguageEntry)o).code);
        return false;
    }
}
