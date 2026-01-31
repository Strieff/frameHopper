package com.FrameHopper.app.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class UserSettings {
    @Getter
    @Setter
    private static UserSettings instance;

    private Boolean showDeleted = false;
    private Boolean openRecent = false;
    private Boolean useDefaultLanguage = true;
    private Boolean settingsWarnings = true;
    private int recentId = -1;
    private String recentExportPath = null;
    private String language = "en";
}
