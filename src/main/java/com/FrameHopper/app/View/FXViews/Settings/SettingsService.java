package com.FrameHopper.app.View.FXViews.Settings;

import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.Service.VideoService;
import com.FrameHopper.app.View.Elements.Language.LanguageEntry;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.FrameHopper.app.View.Elements.UpdateTableEvent.UpdateTableEventDispatcher;
import com.FrameHopper.app.settings.UserSettingsService;
import com.FrameHopper.app.View.FXViews.MainView.MainViewService;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SettingsService {
    private final UserSettingsService userSettingsService;
    private final MainViewService mainViewService;
    private final VideoService videoService;

    @Getter
    private List<LanguageEntry> languages = new ArrayList<>();

    public SettingsService(
            UserSettingsService userSettingsService,
            MainViewService mainViewService,
            VideoService videoService,
            LanguageManager languageManager
    ) {
        this.userSettingsService = userSettingsService;
        this.mainViewService = mainViewService;
        this.videoService = videoService;

        var languageMap = languageManager.getLanguageMap();
        for(var e : languageMap.keySet())
            languages.add(new LanguageEntry(e, languageMap.get(e)));
    }

    public Video getCurrentVideo() {
        return videoService.getById(mainViewService.getCurrentId());
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
