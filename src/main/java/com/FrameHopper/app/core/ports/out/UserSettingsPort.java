package com.FrameHopper.app.core.ports.out;

public interface UserSettingsPort {
    void setLanguage(String code);
    String getLanguage();

    void setOpenRecent(boolean openRecent);
    boolean openRecent();

    void setUseRecentExportPath(boolean useRecentExportPath);
    boolean useRecentExportPath();

    void setRecentExportPath(String path);
    String getRecentExportPath();

    void setRecentlyOpenId(int id);
    int getRecentlyOpenId();

    void changeShowHidden(boolean showHidden);
    boolean showHidden();

    void setUseDefaultLanguageForExport(boolean useDefaultLanguageForExport);
    boolean useDefaultLanguageForExport();

    void setShowWarnings(boolean showWarnings);
    boolean showWarnings();
}
