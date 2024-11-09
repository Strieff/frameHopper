package com.example.engineer.View.Elements;

import javafx.fxml.FXMLLoader;
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
}
