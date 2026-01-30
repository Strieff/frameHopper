package com.FrameHopper.app.settings;

import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.FileWriter;

@Component
@DependsOn("UserSettings")
public class UserSettingsService {
    UserSettings userSettings;

    public UserSettingsService() {
        this.userSettings = UserSettings.getInstance();
    }

    public void setRecentId(int id){
        userSettings.setRecentId(id);
        save();
    }

    public void setShowHidden(boolean showHidden){
        userSettings.setShowDeleted(showHidden);
        save();
    }

    public void setOpenRecent(boolean openRecent){
        userSettings.setOpenRecent(openRecent);
        save();
    }

    public void setExportRecent(String path){
        userSettings.setRecentExportPath(path);
        save();
    }

    public void setUseDefaultLanguage(boolean useDefaultLanguage){
        userSettings.setUseDefaultLanguage(useDefaultLanguage);
        save();
    }

    public void setLanguage(String language){
        userSettings.setLanguage(language);
        LanguageManager.changeLanguage(language);
        save();
    }

    public void setSettingsWarnings(boolean settingsWarnings){
        userSettings.setSettingsWarnings(settingsWarnings);
        save();
    }

    public boolean openRecent(){
        return userSettings.getOpenRecent();
    }

    public boolean ShowHidden(){
        return userSettings.getShowDeleted();
    }

    public boolean useDefaultLanguage(){
        return userSettings.getUseDefaultLanguage();
    }

    public int getRecentId(){
        return userSettings.getRecentId();
    }

    public String getExportPath(){
        return userSettings.getRecentExportPath().isBlank() ? "" : userSettings.getRecentExportPath();
    }

    public String getLanguage(){
        return userSettings.getLanguage();
    }

    public boolean showSettingsWarning() {
        return userSettings.getSettingsWarnings();
    }

    private void save(){
        try(FileWriter writer = new FileWriter("settings/user settings.json")){
            writer.write(new ObjectMapper().writeValueAsString(userSettings));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}