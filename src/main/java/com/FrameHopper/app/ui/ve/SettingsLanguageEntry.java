package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXIconLoader;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class SettingsLanguageEntry {
    private final String code;
    private final Image flagIcon;
    @Setter
    private String name;

    public SettingsLanguageEntry(String code, String name) {
        this.code = code;
        this.name = name;
        this.flagIcon = FXIconLoader.getFlagIcon(code+".png");
    }

    public void setLanguage() {
        setName(LanguageManager.getLanguageName(code));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SettingsLanguageEntry that)) return false;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
