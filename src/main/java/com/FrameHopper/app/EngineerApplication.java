package com.FrameHopper.app;

import com.FrameHopper.app.Service.VideoService;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.adapters.settings.UserSettings;
import com.FrameHopper.app.adapters.settings.UserSettingsAdapter;
import com.FrameHopper.app.ui.FXMLViewLoader;
import com.FrameHopper.app.View.Elements.OpenVideo.OpenVideoEventDispatcher;
import com.FrameHopper.app.ui.UIManager;
import com.FrameHopper.app.ui.language.I18n;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;
import java.net.Socket;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.FrameHopper.app.adapters.persistence.repository")
@EntityScan(basePackages = {"com.FrameHopper.app.adapters.persistence.entities"})
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
            var settings = context.getBean(UserSettingsAdapter.class);
            I18n.setLocale(settings.getLanguage());

            context.getBean(UIManager.class).openMain(primaryStage);

            closeLoadingWindow();

            if (settings.openRecent())
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
        if(UserSettings.getInstance().getRecentlyOpenedId() == -1)
            return;

        VideoService videoService = context.getBean(VideoService.class);
        var video = videoService.getById(UserSettings.getInstance().getRecentlyOpenedId());

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
