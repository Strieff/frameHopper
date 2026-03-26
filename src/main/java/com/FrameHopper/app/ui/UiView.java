package com.FrameHopper.app.ui;

import com.FrameHopper.app.ui.language.I18n;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

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

        return target instanceof TextInputControl;
    }

    protected void addEventFilter(Node node) {
        node.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    protected void bind(Labeled node, String key, Object... args) {
        node.textProperty().bind(I18n.bind(key, args));
    }

    protected void bind(TextInputControl node, String key, Object... args) {
        node.promptTextProperty().bind(I18n.bind(key, args));
    }

    protected void bind(TableColumn<?, ?> column, String key, Object... args) {
        column.textProperty().bind(I18n.bind(key, args));
    }

    protected String getText(String key, Object... args) {
        return I18n.tr(key, args);
    }

    protected abstract void addKeybinds();
    protected abstract void close();
}
