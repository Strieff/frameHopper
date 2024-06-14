package com.example.engineer;

import com.example.engineer.FrameProcessor.FrameProcessorClient;
import com.example.engineer.Model.UserSettings;
import com.example.engineer.Service.SettingsService;
import com.example.engineer.View.FrameHopperView;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.engineer.Repository")
@EntityScan(basePackages = {"com.example.engineer.Model"})
public class EngineerApplication {
    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(EngineerApplication.class).headless(false);
        ConfigurableApplicationContext context = builder.run(args);

        try{
            UserSettings us = context.getBean(SettingsService.class).getUserSettings();

            if(us==null){
                us = UserSettings.builder()
                        .showDeleted(false)
                        .openRecent(false)
                        .build();

                FrameHopperView.USER_SETTINGS = us;

                context.getBean(SettingsService.class).createUserSettings(us);
            }else{
                if(us.getOpenRecent() == null)
                    us.setOpenRecent(false);
                FrameHopperView.USER_SETTINGS = us;
            }

            context.getBean(FrameHopperView.class).setUpButtonViews();

            context.getBean(FrameProcessorClient.class).connect();
        }finally {
            try {
                new Socket("localhost",65444);
            } catch (IOException e) {
                System.out.println("Loading frame closed. FrameHopper is running!");
            }
        }

        //open recent
        if(FrameHopperView.USER_SETTINGS.getOpenRecent() && FrameHopperView.USER_SETTINGS.getRecentPath()!=null){
            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Recently opened: " + FrameHopperView.USER_SETTINGS.getRecentPath() + "\nOpen recent?",
                    "Open recent video",
                    JOptionPane.YES_NO_OPTION
            );

            if(response==JOptionPane.YES_OPTION){
                File recentFile = new File(FrameHopperView.USER_SETTINGS.getRecentPath());
                if(recentFile.exists())
                    context.getBean(FrameHopperView.class).openRecentVideo(FrameHopperView.USER_SETTINGS.getRecentPath());
                else
                    JOptionPane.showMessageDialog(null, "The file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }
}
