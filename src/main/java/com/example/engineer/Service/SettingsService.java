package com.example.engineer.Service;

import com.example.engineer.Model.UserSettings;
import com.example.engineer.Repository.SettingsRepository;
import com.example.engineer.View.Elements.UserSettingsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsService implements ApplicationContextAware {
    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    @Autowired
    SettingsRepository settingsRepository;

    public void changeSettings(){
        settingsRepository.save(ctx.getBean(UserSettingsManager.class).getUserSettings());
    }

    public UserSettings getUserSettings(){
        List<UserSettings> us = settingsRepository.findAll();

        return us.isEmpty() ? null : us.get(0);
    }

    public void createUserSettings(UserSettings userSettings){
        settingsRepository.save(userSettings);
    }
}
