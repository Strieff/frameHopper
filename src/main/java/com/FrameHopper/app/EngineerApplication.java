package com.FrameHopper.app;

import com.FrameHopper.app.Service.VideoService;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXMLViewLoader;
import com.FrameHopper.app.View.Elements.OpenVideo.OpenVideoEventDispatcher;
import com.FrameHopper.app.settings.UserSettingsService;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;
import java.net.Socket;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.FrameHopper.app.Repository")
@EntityScan(basePackages = {"com.FrameHopper.app.Model"})
public class EngineerApplication extends Application {
    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(EngineerApplication.class).headless(false);
        context = builder.run();
        new FXMLViewLoader(context);
    }

    @Override
    public void start(Stage primaryStage) {
        try{
            var loader = FXMLViewLoader.getView("MainViewModel");
            Parent root = loader.load();
            primaryStage.setTitle("FrameHopper");
            primaryStage.setScene(new Scene(root, 1200, 900));
            primaryStage.show();

            closeLoadingWindow();

            if (context.getBean(UserSettingsService.class).openRecent())
                openRecent(context);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        context.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void openRecent(ConfigurableApplicationContext context){
        UserSettingsService userSettingsService = context.getBean(UserSettingsService.class);
        VideoService videoService = context.getBean(VideoService.class);
        var video = videoService.getById(userSettingsService.getRecentId());

        if(userSettingsService.getRecentId() == -1)
            return;

        //TODO: change to dictionary
        if(
                !FXDialogProvider.yesNoDialog(
                "Open recent video",
                String.format("Recently opened: %s\nOpen recent?", video.getName().replace("%20", " ")))
        )
            return;

        OpenVideoEventDispatcher.fireEvent(video.getId());
    }

    private static void closeLoadingWindow(){
        try {
            new Socket("localhost",65444);
        } catch (IOException e) {
            System.out.println("Loading frame closed. FrameHopper is running!");
        }
    }
}
