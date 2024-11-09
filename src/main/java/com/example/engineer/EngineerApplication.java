package com.example.engineer;

import com.example.engineer.FrameProcessor.FrameProcessorClient;
import com.example.engineer.Model.Video;
import com.example.engineer.Service.VideoService;
import com.example.engineer.View.Elements.FXDialogProvider;
import com.example.engineer.View.Elements.FXMLViewLoader;
import com.example.engineer.View.Elements.FileChooserProvider;
import com.example.engineer.View.Elements.UserSettingsManager;
import com.example.engineer.View.FXViews.MainView.MainViewController;
import com.example.engineer.View.ViewModel.MainApplication.FrameHopperView;
import com.example.engineer.View.ViewModel.Settings.SettingsView;
import com.example.engineer.View.ViewModel.TagManagerView.TagManagerView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.engineer.Repository")
@EntityScan(basePackages = {"com.example.engineer.Model"})
public class EngineerApplication extends Application {
    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(EngineerApplication.class).headless(false);
        context = builder.run();
        new FXMLViewLoader(context);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var loader =  FXMLViewLoader.getView("MainViewModel");
        Parent root = loader.load();
        primaryStage.setTitle("FrameHopper");
        primaryStage.setScene(new Scene(root,1200,900));
        primaryStage.show();

        checkNecessaryFiles();
        context.getBean(SettingsView.class).setUpView();
        context.getBean(TagManagerView.class).setUpView(context.getBean(FrameHopperView.class));
        context.getBean(FrameProcessorClient.class).connect();
        closeLoadingWindow();

        if(context.getBean(UserSettingsManager.class).openRecent())
            if(openRecent(context,primaryStage)){
                MainViewController controller = loader.getController();
                controller.openRecent(context.getBean(UserSettingsManager.class).getRecentPath());
            }
    }

    @Override
    public void stop() {
        context.close();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void checkNecessaryFiles(){
        if(Files.exists(Path.of("cache")))
            new File("cache").mkdirs();

    }

    private static boolean openRecent(ConfigurableApplicationContext context,Stage stage){
        UserSettingsManager userSettings = context.getBean(UserSettingsManager.class);

        if(userSettings.getRecentPath() == null)
            return false;

        var openRecent = FXDialogProvider.YesNoDialog(
                String.format("Recently opened: %s\nOpen recent?",userSettings.getRecentPath()),
                "Open recent video"
        );

        if(openRecent){
            var recentFile = new File(userSettings.getRecentPath());
            if(!recentFile.exists()) {
                if (FXDialogProvider.YesNoDialog(String.format("No file found: %s\nSet new path?", userSettings.getRecentPath())))
                    reelPath(context, userSettings.getRecentPath(), stage);
                else
                    return false;
            }

            return true;
        }

        return false;
    }

    private static void reelPath(ConfigurableApplicationContext context,String oldPath,Stage stage){
        try {
            var newPath = FileChooserProvider.videoFileChooser(stage);
            if(newPath.isBlank())
                throw new RuntimeException("No file selected");

            VideoService videoService = context.getBean(VideoService.class);
            Video video = videoService.getByPath(oldPath);
            video.setPath(newPath);
            video = videoService.saveVideo(video);
            UserSettingsManager userSettings = context.getBean(UserSettingsManager.class);
            userSettings.setRecentPath(video.getPath());
        } catch (Exception e) {
            FXDialogProvider.errorDialog(e.getMessage());
        }
    }

    private static void closeLoadingWindow(){
        try {
            new Socket("localhost",65444);
        } catch (IOException e) {
            System.out.println("Loading frame closed. FrameHopper is running!");
        }
    }
}
