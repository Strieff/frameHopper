package com.FrameHopper.app.adapters.settings;

import com.FrameHopper.app.core.ports.out.UserSettingsPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.FileWriter;

@Component
@DependsOn("UserSettings")
public class UserSettingsAdapter implements UserSettingsPort {
    private final UserSettings settings;

    public UserSettingsAdapter() {
        this.settings = UserSettings.getInstance();
    }

    @Override
    public void setLanguage(String code) {
        settings.setLanguage(code);
        save();
    }

    @Override
    public String getLanguage() {
        return settings.getLanguage();
    }

    @Override
    public void setOpenRecent(boolean openRecent) {
        settings.setOpenRecent(openRecent);
        save();
    }

    @Override
    public boolean openRecent() {
        return settings.getOpenRecent();
    }

    @Override
    public void setRecentExportPath(String path) {
        settings.setRecentExportPath(path);
        save();
    }

    @Override
    public String getRecentExportPath() {
        return settings.getRecentExportPath();
    }

    @Override
    public void setRecentlyOpenId(int id) {
        settings.setRecentlyOpenedId(id);
        save();
    }

    @Override
    public int getRecentlyOpenId() {
        return settings.getRecentlyOpenedId();
    }

    @Override
    public void changeShowHidden(boolean showHidden) {
        settings.setShowHidden(showHidden);
        save();
    }

    @Override
    public boolean showHidden() {
        return settings.getShowHidden();
    }

    @Override
    public void setUseDefaultLanguageForExport(boolean useDefaultLanguageForExport) {
        settings.setUseDefaultLanguageForExport(useDefaultLanguageForExport);
        save();
    }

    @Override
    public boolean useDefaultLanguageForExport() {
        return settings.getUseDefaultLanguageForExport();
    }

    @Override
    public void setShowWarnings(boolean showWarnings) {
        settings.setShowWarnings(showWarnings);
        save();
    }

    @Override
    public boolean showWarnings() {
        return settings.getShowWarnings();
    }

    private void save(){
        try(FileWriter writer = new FileWriter("settings/user settings.json")){
            writer.write(new ObjectMapper().writeValueAsString(settings));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
