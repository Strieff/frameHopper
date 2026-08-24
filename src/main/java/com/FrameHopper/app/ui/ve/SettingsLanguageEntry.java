package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.ui.language.I18n;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

@Getter
public class SettingsLanguageEntry {
    private final String code;
    private final Image flagIcon;
    StringProperty languageNameProperty = new SimpleStringProperty();

    public SettingsLanguageEntry(String code, Image flagIcon) {
        this.code = code;
        this.flagIcon = flagIcon;

        languageNameProperty.setValue(getLanguageName(code));
        languageNameProperty.bind(
                Bindings.createStringBinding(
                        () -> getLanguageName(this.code),
                        I18n.localeProperty()
                )
        );
    }

    private String getLanguageName(String code) {
        return Locale.of(code).getDisplayLanguage(I18n.getLocale());
    }

    public String getLanguageName() {
        return languageNameProperty.get();
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
