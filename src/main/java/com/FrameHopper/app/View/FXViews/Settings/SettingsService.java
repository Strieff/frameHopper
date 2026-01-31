package com.FrameHopper.app.View.FXViews.Settings;

import com.FrameHopper.app.View.Elements.Language.LanguageEntry;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.FrameHopper.app.settings.UserSettingsService;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SettingsService {
    private final UserSettingsService userSettingsService;

    @Getter
    private final List<LanguageEntry> languages = new ArrayList<>();

    public SettingsService(
            UserSettingsService userSettingsService,
            LanguageManager languageManager
    ) {
        this.userSettingsService = userSettingsService;

        var languageMap = languageManager.getLanguageMap();
        for(var e : languageMap.keySet())
            languages.add(new LanguageEntry(e, languageMap.get(e)));
    }

    public void changeShowHidden(boolean checked){
        userSettingsService.setShowHidden(checked);
        UpdateTableEventDispatcher.fireEvent();
    }

    public LanguageEntry getCurrentLanguage() {
        return languages.stream()
                .filter(e -> e.getCode().equals(userSettingsService.getLanguage()))
                .findFirst()
                .orElse(null);
    }
}
