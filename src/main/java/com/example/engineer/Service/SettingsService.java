package com.example.engineer.Service;

import com.example.engineer.Model.UserSettings;
import com.example.engineer.Repository.SettingsRepository;
import com.example.engineer.View.FrameHopperView;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsService {
    @Autowired
    SettingsRepository settingsRepository;

    public void changeSettings(){
        settingsRepository.save(FrameHopperView.USER_SETTINGS);
    }

    public UserSettings getUserSettings(){
        List<UserSettings> us = settingsRepository.findAll();

        if(us.isEmpty())
            return null;
        else
            return us.get(0);
    }

    public void createUserSettings(UserSettings userSettings){
        settingsRepository.save(userSettings);
    }
}
