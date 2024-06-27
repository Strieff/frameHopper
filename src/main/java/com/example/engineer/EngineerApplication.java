package com.example.engineer;

import com.example.engineer.FrameProcessor.FrameProcessorClient;
import com.example.engineer.View.Elements.UserSettingsManager;
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

//TODO: file path reeling
//TODO: open files with unicode characters - hard link in python
//TODO: remove absolute path in getting video info, path already exists after initial movie connection

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.engineer.Repository")
@EntityScan(basePackages = {"com.example.engineer.Model"})
public class EngineerApplication {
    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(EngineerApplication.class).headless(false);
        ConfigurableApplicationContext context = builder.run(args);

        try{
            context.getBean(UserSettingsManager.class).createUserSettings();

            context.getBean(FrameHopperView.class).setUpButtonViews();

            context.getBean(FrameProcessorClient.class).connect();

            if(!isPathValid())
                throw new Exception();
        }catch (Exception e){
            JOptionPane.showMessageDialog(
                    null,
                    new JLabel("<html><center>Path Invalid!<br>"
                            + System.getProperty("user.dir") + " contains non-standard characters.<br>"
                            + "Please run the program again after resolving the issue"),
                    "ERROR",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(0);
        }finally{
            try {
                new Socket("localhost",65444);
            } catch (IOException e) {
                System.out.println("Loading frame closed. FrameHopper is running!");
            }
        }

        openRecent(context);
    }

    private static void openRecent(ConfigurableApplicationContext context){
        UserSettingsManager userSettings = context.getBean(UserSettingsManager.class);

        if(userSettings.OpenRecent() && userSettings.getRecentPath()!=null){
            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Recently opened: " + userSettings.getRecentPath() + "\nOpen recent?",
                    "Open recent video",
                    JOptionPane.YES_NO_OPTION
            );

            if(response==JOptionPane.YES_OPTION){
                File recentFile = new File(userSettings.getRecentPath());
                if(recentFile.exists())
                    context.getBean(FrameHopperView.class).openRecentVideo(recentFile);
                else
                    JOptionPane.showMessageDialog(null, "The file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    private static boolean isPathValid(){
        String path = System.getProperty("user.dir");
        return path.matches("\\A\\p{ASCII}*\\z");
    }
}
