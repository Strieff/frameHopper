package com.example.engineer.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
public class UserSettings {
    @Getter
    @Setter
    private static UserSettings instance;
    private Boolean showDeleted;
    private Boolean openRecent;
    private Boolean useDefaultLanguage;
    private Boolean settingsWarnings;
    private String recentPath;
    private String recentExportPath;
    private String language;
}
