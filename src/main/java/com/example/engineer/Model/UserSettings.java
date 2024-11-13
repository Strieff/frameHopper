package com.example.engineer.Model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@ToString
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
