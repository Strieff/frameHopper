package com.example.engineer.DBActions;

import com.example.engineer.Model.UserSettings;
import com.example.engineer.Service.SettingsService;

import java.util.concurrent.CompletableFuture;

public class SaveSettingsAction extends DBAction{
    SettingsService settingsService;
    UserSettings userSettings;

    public SaveSettingsAction(SettingsService settingsService) {
        super();
        this.settingsService = settingsService;
    }

    public SaveSettingsAction(SettingsService settingsService, UserSettings userSettings) {
        super();
        this.settingsService = settingsService;
        this.userSettings = userSettings;
    }

    @Override
    public void run() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            settingsService.changeSettings();
        },executor);

        future.join();

        shutdown();
    }
}
