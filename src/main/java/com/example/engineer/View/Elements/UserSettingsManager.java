package com.example.engineer.View.Elements;

import com.example.engineer.DBActions.SaveSettingsAction;
import com.example.engineer.Model.UserSettings;
import com.example.engineer.Service.SettingsService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserSettingsManager {
    @Autowired
    SettingsService settingsService;
    @Getter
    UserSettings userSettings;

    public void createUserSettings(){
        userSettings = settingsService.getUserSettings();
        if(userSettings == null){
            userSettings = UserSettings.builder()
                    .showDeleted(false)
                    .openRecent(false)
                    .build();

            settingsService.createUserSettings(userSettings);
        }
    }

    public void setRecentPath(String path){
        userSettings.setRecentPath(path);
        //run();
    }

    public void setShowHidden(boolean showHidden){
        userSettings.setShowDeleted(showHidden);
        //run();
    }

    public void setOpenRecent(boolean openRecent){
        userSettings.setOpenRecent(openRecent);
        //run();
    }

    public void setExportRecent(String path){
        userSettings.setRecentExportPath(path);
        //run();
    }

    public boolean OpenRecent(){
        return userSettings.getOpenRecent();
    }

    public boolean ShowHidden(){
        return userSettings.getShowDeleted();
    }

    public String getRecentPath(){
        return userSettings.getRecentPath();
    }

    public String getExportPath(){
        return userSettings.getRecentExportPath();
    }

    public void save(){
        new SaveSettingsAction(settingsService,userSettings).run();
    }

}
