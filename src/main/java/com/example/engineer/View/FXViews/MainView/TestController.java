package com.example.engineer.View.FXViews.MainView;


import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class TestController {
    @FXML
    private Canvas frameCanvas;
    @FXML
    private BorderPane mainView;

    @Autowired
    TestService viewService;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    @FXML
    public void initialize(){
        keyActions.put(new KeyCodeCombination(KeyCode.COMMA), this::onCommaPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.PERIOD), this::onPeriodPressed);

        mainView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);
    }

    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    private void onPeriodPressed() {
        System.out.println("onPeriodPressed");
        viewService.moveRight();
        System.out.println("moving right");
    }

    private void onCommaPressed() {
        System.out.println("onCommaPressed");
        viewService.moveLeft();
        System.out.println("moving left");
    }

    public void init(File file){
        viewService.onInit(file, frameCanvas);
    }


}
