package com.FrameHopper.app.ui.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class UserSettings {
    @Getter
    @Setter
    private static UserSettings instance;

    private Boolean showHidden = false;
    private Boolean openRecent = false;
    private Boolean useRecentExportPath = false;
    private Boolean useDefaultLanguageForExport = true;
    private Boolean showWarnings = true;

    private int recentlyOpenedId = -1;

    private String recentExportPath = null;
    private String language = "en";
}
