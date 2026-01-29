package com.FrameHopper.app.View.Elements.FXElementsProviders;

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
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("viewModels/"+viewName+".fxml"));
        loader.setControllerFactory(context::getBean);
        return loader;
    }

    public static FXMLLoader getView(String name){
        try {
            return instance.get(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FXMLLoader getView(String viewName, String windowName, Node windowNode){
        try {
            var loader = instance.get(viewName);

            //load scene
            Parent root = loader.load();
            var scene = new Scene(root);

            //new stage
            var secondaryStage = new Stage();
            secondaryStage.setScene(scene);
            secondaryStage.setTitle(windowName);

            //make it a modal window
            secondaryStage.initOwner(windowNode.getScene().getWindow());
            secondaryStage.show();

            return loader;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
