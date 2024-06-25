package com.example.engineer.DBActions;

import com.example.engineer.Service.SettingsService;

import java.util.concurrent.CompletableFuture;

public class SaveSettingsAction extends DBAction{
    SettingsService settingsService;

    public SaveSettingsAction(SettingsService settingsService) {
        super();
        this.settingsService = settingsService;
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
