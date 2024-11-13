package com.example.engineer.View.Elements.DataManagers;

import com.example.engineer.Model.UserSettings;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.google.gson.Gson;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.FileWriter;

@Component
@DependsOn("UserSettings")
public class UserSettingsManager{

    UserSettings userSettings;

    public UserSettingsManager() {
        this.userSettings = UserSettings.getInstance();
    }

    public void setRecentPath(String path){
        userSettings.setRecentPath(path);
        save();
    }

    public void setShowHidden(boolean showHidden){
        userSettings.setShowDeleted(showHidden);
    }

    public void setOpenRecent(boolean openRecent){
        userSettings.setOpenRecent(openRecent);
        save();
    }

    public void setExportRecent(String path){
        userSettings.setRecentExportPath(path);
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

    public String getRecentPath(){
        return userSettings.getRecentPath();
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

    public void save(){
        //save to file
        try(FileWriter writer = new FileWriter("settings/user settings.json")){
            new Gson().toJson(userSettings, writer);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}