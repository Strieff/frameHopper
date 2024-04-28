package com.example.engineer;

import com.example.engineer.FrameProcessor.FrameCache;
import com.example.engineer.FrameProcessor.FrameProcessorClient;
import com.example.engineer.FrameProcessor.FrameProcessorHandler;
import com.example.engineer.Model.UserSettings;
import com.example.engineer.Service.SettingsService;
import com.example.engineer.View.FrameHopperView;
import com.example.engineer.View.smallViews.LoadingView;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {HibernateJpaAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "com.example.engineer.Repository")
@EntityScan(basePackages = {"com.example.engineer.Model"})
public class EngineerApplication {
    public static void main(String[] args) {
        //FrameProcessorHandler.runServer();

        new Thread(() -> {
            LoadingView loadingView = new LoadingView("LOADING...");

            try {
                Thread.sleep(14000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            loadingView.dispose();
        }).start();



        SpringApplicationBuilder builder = new SpringApplicationBuilder(EngineerApplication.class).headless(false);
        ConfigurableApplicationContext context = builder.run(args);

        context.getBean(FrameProcessorClient.class).connect();
        //context.getBean(FrameProcessorClient.class).send("0;xd");
        //context.getBean(FrameProcessorClient.class).send("-1;xd");

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
    }
}
