package com.FrameHopper.app.ui;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

public abstract class UiView {
    protected final Map<KeyCombination, Runnable> keyActions = new HashMap<>();

    protected void handleKeyPressed(KeyEvent event) {
        if(isTyping(event)) return;

        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    private boolean isTyping(KeyEvent event) {
        Object target = event.getTarget();

        return target instanceof TextInputControl
                || target instanceof ComboBox
                || target instanceof ToggleButton;
    }

    protected void addEventFilter(Node node) {
        node.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    protected abstract void addKeybinds();
    protected abstract void close();
}
