package com.FrameHopper.app.ui;

import com.FrameHopper.app.ui.language.I18n;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class FXMLViewLoader {
    private static FXMLViewLoader instance;

    private final ConfigurableApplicationContext context;

    public FXMLViewLoader(ConfigurableApplicationContext context) {
        this.context = context;
        instance = this;
    }

    private FXMLLoader get(String viewName) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(String.format("viewModels/%s.fxml", viewName)));
        loader.setControllerFactory(context::getBean);
        return loader;
    }

    public static FXMLLoader getMainView(String viewName, String windowKey, Stage primaryStage){
        try {
            var loader = instance.get(viewName);
            Parent root = loader.load();
            bind(primaryStage, windowKey);
            primaryStage.setScene(new Scene(root, 1200, 900));
            primaryStage.show();

            return loader;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FXMLLoader getView(String viewName, String windowKey, Node windowNode){
        try {
            var loader = instance.get(viewName);

            //load scene
            Parent root = loader.load();
            var scene = new Scene(root);

            //new stage
            var secondaryStage = new Stage();
            secondaryStage.setScene(scene);
            secondaryStage.setTitle(windowKey);
            bind(secondaryStage, windowKey);

            //make it a modal window
            secondaryStage.initOwner(windowNode.getScene().getWindow());
            secondaryStage.show();

            return loader;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void bind(Stage stage, String key, Object... args) {
        stage.titleProperty().bind(I18n.bind(key, args));
    }
}
