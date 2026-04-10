package com.FrameHopper.app.ui.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class UserSettingsConfig {
    private static final String SETTINGS_PATH = "settings/user settings.json";

    @SneakyThrows
    @Bean(name = "UserSettings")
    public UserSettings getUserSettings() {
        String userSettings;
        try {
            Path path = Path.of(SETTINGS_PATH).toAbsolutePath().normalize();
            var parent = path.getParent();
            if(!Files.exists(parent))
                Files.createDirectories(parent);

            if(Files.notExists(path)){
                try(FileWriter writer = new FileWriter(path.toFile())){
                    var settings = new UserSettings();
                    writer.write(new ObjectMapper().writeValueAsString(settings));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            userSettings = new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ObjectMapper().readValue(userSettings, UserSettings.class);
    }
}
