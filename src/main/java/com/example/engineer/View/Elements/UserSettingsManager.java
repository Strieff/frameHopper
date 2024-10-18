package com.example.engineer.View.Elements;

import com.example.engineer.Model.UserSettings;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.FileWriter;

@Component
@DependsOn("UserSettings")
public class UserSettingsManager{
    @Autowired
    LanguageManager languageManager;

    UserSettings userSettings;

    public UserSettingsManager() {
        this.userSettings = UserSettings.getInstance();
    }

    public void setRecentPath(String path){
        userSettings.setRecentPath(path);
    }

    public void setShowHidden(boolean showHidden){
        userSettings.setShowDeleted(showHidden);
    }

    public void setOpenRecent(boolean openRecent){
        userSettings.setOpenRecent(openRecent);
    }

    public void setExportRecent(String path){
        userSettings.setRecentExportPath(path);
    }

    public void setUseDefaultLanguage(boolean useDefaultLanguage){
        userSettings.setUseDefaultLanguage(useDefaultLanguage);
    }

    public void setLanguage(String language){
        userSettings.setLanguage(language);
        languageManager.changeLanguage(language);
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
        return userSettings.getRecentExportPath();
    }

    public String getLanguage(){
        return userSettings.getLanguage();
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