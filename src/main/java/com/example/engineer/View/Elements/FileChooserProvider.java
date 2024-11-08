package com.example.engineer.View.Elements;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FileChooserProvider {
    public static String textFileChooser(Stage stage) throws Exception{
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Get txt tag file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt","*.csv"));
        File file = fileChooser.showOpenDialog(stage);

        if(file != null)
            return file.getAbsolutePath();
        else
            throw new Exception("File not selected");
    }

    public static String videoFileChooser(Stage stage) throws Exception{
        var fileExtensions = new String[]{
                "*.gif","*.webm","*.mkv","*.flv","*.vob",
                "*.ogv","*.ogg","*.rrc","*.gifv","*.mng",
                "*.mov","*.avi","*.qt","*.wmv","*.yuv",
                "*.rm","*.asf","*.amv","*.mp4","*.m4p",
                "*.m4v","*.mpg","*.mp2","*.mpeg","*.mpe",
                "*.mpv","*.m4v","*.svi","*.3gp","*.3g2",
                "*.mxf","*.roq","*.nsv","*.flv","*.f4v",
                "*.f4p","*.f4a","*.f4b","*.mod"};

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Get video file");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video File",fileExtensions));

        var file = fileChooser.showOpenDialog(stage);

        if(file != null)
            return file.getAbsolutePath();
        else
            throw new Exception("File not selected");
    }
}
