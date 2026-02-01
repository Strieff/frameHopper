package com.FrameHopper.app.core.ports.out;

import com.FrameHopper.app.adapters.settings.UserSettingsAdapter;

public interface UserSettingsPort {
    UserSettingsAdapter getUserSettingsService();
    void setLanguage(String code);
    void setOpenRecent(boolean openRecent);
    void setRecentExportPath(String path);
    void setRecentlyOpenId(int id);
    void showHidden(boolean showHidden);
    void setUseDefaultLanguageForExport(boolean useDefaultLanguageForExport);
}
