package com.FrameHopper.app.ui.dialog;

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class FileChooserProvider {
    public static String textFileChooser(Stage stage) throws IOException {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File"); //TODO: change to dict
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt", "*.csv"));
        var file = fileChooser.showOpenDialog(stage);

        if (file == null || !file.exists())
            throw new IOException("File not found!");

        return file.getAbsolutePath();
    }

    public static String videoFileChooser(Stage stage) throws IOException {
        var fileExtensions = new String[]{
                "*.gif", "*.webm", "*.mkv", "*.flv", "*.vob",
                "*.ogv", "*.ogg", "*.rrc", "*.gifv", "*.mng",
                "*.mov", "*.avi", "*.qt", "*.wmv", "*.yuv",
                "*.rm", "*.asf", "*.amv", "*.mp4", "*.m4p",
                "*.m4v", "*.mpg", "*.mp2", "*.mpeg", "*.mpe",
                "*.mpv", "*.m4v", "*.svi", "*.3gp", "*.3g2",
                "*.mxf", "*.roq", "*.nsv", "*.flv", "*.f4v",
                "*.f4p", "*.f4a", "*.f4b", "*.mod"
        };

        var fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video File", fileExtensions));

        var file = fileChooser.showOpenDialog(stage);

        if (file == null || !file.exists())
            throw new IOException("File not found!");

        return file.getAbsolutePath();
    }

    public static String locationChooser(Stage stage, String openPath) throws IOException {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Directory");

        if(openPath != null && !openPath.isBlank())
            directoryChooser.setInitialDirectory(new File(openPath));

        var selectedLocation = directoryChooser.showDialog(stage);
        if (selectedLocation == null || !selectedLocation.exists() ||!selectedLocation.isDirectory())
            throw new IOException("Invalid directory!");

        return selectedLocation.getAbsolutePath();
    }

    public static String locationWithNameChooser(Stage stage, String openPath) throws IOException {
        var name = FXDialogProvider.inputDialog();

        if (name == null || name.isBlank()) throw new IOException("File name can't be empty!");

        var directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Directory");

        if(openPath != null && !openPath.isBlank())
            directoryChooser.setInitialDirectory(new File(openPath));

        var selectedLocation = directoryChooser.showDialog(stage);
        if (selectedLocation == null || !selectedLocation.exists() ||!selectedLocation.isDirectory())
            throw new IOException("Invalid directory!");

        var finalPath = selectedLocation.getAbsolutePath() + File.separator + name;

        while (new File(finalPath).exists()) {
            var res = FXDialogProvider.customDialog(
                    Dictionary.get("dialog.export.exists"),
                    0,
                    Dictionary.get("cancel"),
                    Dictionary.get("dialog.export.option.rename"),
                    Dictionary.get("dialog.export.option.overwrite")
            );

            switch (res) {
                case 0:
                    FXDialogProvider.messageDialog(Dictionary.get("cancelled"));
                    break;
                case 1:
                    name = FXDialogProvider.inputDialog();
                    if (name.isBlank()) FXDialogProvider.errorDialog(Dictionary.get("error.export.no-name"));
                    finalPath = selectedLocation.getAbsolutePath() + File.separator + name;
                    break;
            }

            if (res == 2) break;
        }

        return finalPath;
    }

    public static String locationFileSaveChooser(Stage stage, String extension, String openPath) throws IOException {
        var name = FXDialogProvider.inputDialog();

        if (name == null || name.isBlank()) throw new IOException("File name can't be empty!");

        var location =  locationChooser(stage, openPath);
        var saveDirectory = new File(location + File.separator + name + extension);

        while (saveDirectory.exists()) {
            var res = FXDialogProvider.customDialog(
                    Dictionary.get("dialog.export.exists"),
                    0,
                    Dictionary.get("cancel"),
                    Dictionary.get("dialog.export.option.rename"),
                    Dictionary.get("dialog.export.option.overwrite")
            );

            switch (res) {
                case 0:
                    FXDialogProvider.messageDialog(Dictionary.get("cancelled"));
                    break;
                case 1:
                    name = FXDialogProvider.inputDialog();
                    if (name.isBlank()) FXDialogProvider.errorDialog(Dictionary.get("error.export.no-name"));
                    break;
            }

            if (res == 2) break;
        }

        return new File(location + File.separator + name + extension).getAbsolutePath();
    }
}
