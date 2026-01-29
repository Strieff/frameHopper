package com.FrameHopper.app.View.Elements.Language;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LanguageEntry {
    private final String code;
    private final Image flagIcon;
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
