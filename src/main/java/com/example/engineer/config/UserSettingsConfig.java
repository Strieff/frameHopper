package com.example.engineer.config;

import com.example.engineer.View.Elements.Language.Dictionary;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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

    @SneakyThrows
    @Bean(name = "UserSettings")
    public void getUserSettings() {
        String userSettings;
        try {
            Path path = Path.of(SETTINGS_PATH);

            if(Files.notExists(path)){
                try(FileWriter writer = new FileWriter(String.valueOf(path))){
                    var settings = new UserSettings(
                            false,
                            false,
                            true,
                            true,
                            null,
                            null,
                            "en"
                    );
                    writer.write(new ObjectMapper().writeValueAsString(settings));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            userSettings = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UserSettings.setInstance(new ObjectMapper().readValue(userSettings, UserSettings.class));
    }

    @Bean(name = "SetDictionary")
    @DependsOn("UserSettings")
    public void setDictionary() {
        String code = UserSettings.getInstance().getLanguage();
        new Dictionary(code);
    }
}
