package com.FrameHopper.app.adapters.settings;

import com.FrameHopper.app.core.ports.out.UserSettingsPort;
import org.springframework.stereotype.Component;

@Component
public class UserSettingsAdapter implements UserSettingsPort {
    @Override
    public void setLanguage(String code) {

    }

    @Override
    public String getLanguage() {
        return "";
    }

    @Override
    public void setOpenRecent(boolean openRecent) {

    }

    @Override
    public boolean openRecent() {
        return false;
    }

    @Override
    public void setRecentExportPath(String path) {

    }

    @Override
    public String getRecentExportPath() {
        return "";
    }

    @Override
    public void setRecentlyOpenId(int id) {

    }

    @Override
    public int getRecentlyOpenId() {
        return 0;
    }

    @Override
    public void changeShowHidden(boolean showHidden) {

    }

    @Override
    public boolean showHidden() {
        return false;
    }

    @Override
    public void setUseDefaultLanguageForExport(boolean useDefaultLanguageForExport) {

    }

    @Override
    public boolean useDefaultLanguageForExport() {
        return false;
    }
}
