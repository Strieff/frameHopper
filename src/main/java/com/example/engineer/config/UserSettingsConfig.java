package com.example.engineer.config;

import com.example.engineer.Model.UserSettings;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class UserSettingsConfig {
    private static final String SETTINGS_PATH = "settings/user settings.json";

    @Bean(name = "UserSettings")
    public void getUserSettings() {
        String userSettings;
        try {
            Path path = Path.of(SETTINGS_PATH);

            if(Files.notExists(path)){
                try(FileWriter writer = new FileWriter(String.valueOf(path))){
                    new GsonBuilder()
                            .serializeNulls()
                            .create()
                            .toJson(new UserSettings(
                            false,
                            false,
                            true,
                            true,
                            null,
                            null,
                            "en"
                    ), writer);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            userSettings = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserSettings.setInstance(new Gson().fromJson(userSettings, UserSettings.class));
    }

    @Bean(name = "SetDictionary")
    @DependsOn("UserSettings")
    public void setDictionary() {
        String code = UserSettings.getInstance().getLanguage();
        new Dictionary(code);
    }
}
