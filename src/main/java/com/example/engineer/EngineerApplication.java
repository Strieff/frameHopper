package com.example.engineer;

import com.example.engineer.FrameProcessor.FrameProcessorClient;
import com.example.engineer.Model.UserSettings;
import com.example.engineer.Service.SettingsService;
import com.example.engineer.View.FrameHopperView;
import com.example.engineer.View.smallViews.ApplicationLoadingView;
import com.example.engineer.View.smallViews.StatusView;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.engineer.Repository")
@EntityScan(basePackages = {"com.example.engineer.Model"})
public class EngineerApplication {
    public static void main(String[] args) {
        new Thread(() -> {
            ApplicationLoadingView applicationLoadingView = new ApplicationLoadingView("LOADING...");

            try {
                Thread.sleep(14000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            applicationLoadingView.dispose();
        }).start();



        SpringApplicationBuilder builder = new SpringApplicationBuilder(EngineerApplication.class).headless(false);
        ConfigurableApplicationContext context = builder.run(args);

        {
            UserSettings us = context.getBean(SettingsService.class).getUserSettings();

            if(us==null){
                us = UserSettings.builder()
                        .showDeleted(false)
                        .build();

                FrameHopperView.USER_SETTINGS = us;

                context.getBean(SettingsService.class).createUserSettings(us);
            }else{
                FrameHopperView.USER_SETTINGS = us;
            }
        }

        context.getBean(FrameHopperView.class).setUpButtonViews();

        context.getBean(FrameProcessorClient.class).connect();

        context.getBean(StatusView.class).setUpView();
    }
}
