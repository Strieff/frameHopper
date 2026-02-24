package com.FrameHopper.app.ui;

import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

public abstract class UiView {
    protected final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    protected void handleKeyPressed(KeyEvent event) {
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    protected abstract void addKeybinds();
    protected abstract void close();
}
