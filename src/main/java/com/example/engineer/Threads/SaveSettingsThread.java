package com.example.engineer.Threads;

import com.example.engineer.Service.SettingsService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SaveSettingsThread {
    SettingsService settingsService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SaveSettingsThread(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void start(){
        executorService.execute(() -> {
            settingsService.changeSettings();

            executorService.shutdown();

            try {
                if(!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)){
                    executorService.shutdownNow();
                }
            }catch (Exception e){
                executorService.shutdownNow();
            }
        });
    }
}
