package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.adapters.settings.UserSettingsAdapter;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.ve.SettingsLanguageCell;
import com.FrameHopper.app.ui.ve.SettingsLanguageEntry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class SettingsController extends UiView {
    @FXML
    private BorderPane settingsView;
    @FXML
    private CheckBox showHiddenTagsCheckBox, openRecentCheckBox, languageExportCheckBox, settingsWarningCheckbox;
    @FXML
    private ComboBox<SettingsLanguageEntry> languageBox;

    private final UserSettingsAdapter userSettingsAdapter;

    public SettingsController(UserSettingsAdapter userSettingsAdapter) {
        this.userSettingsAdapter = userSettingsAdapter;
    }

    @FXML
    public void initialize() {
        showHiddenTagsCheckBox.setText(Dictionary.get("settings.user.hidden"));
        showHiddenTagsCheckBox.setSelected(userSettingsAdapter.showHidden());
        showHiddenTagsCheckBox.setOnAction(e -> {
            var selected = showHiddenTagsCheckBox.isSelected();
            showHiddenTagsCheckBox.setSelected(selected);
            userSettingsAdapter.changeShowHidden(selected);
        });

        openRecentCheckBox.setText(Dictionary.get("settings.user.recent"));
        openRecentCheckBox.setSelected(userSettingsAdapter.openRecent());
        openRecentCheckBox.setOnAction(e -> {
            var selected = openRecentCheckBox.isSelected();
            openRecentCheckBox.setSelected(selected);
            userSettingsAdapter.setOpenRecent(selected);
        });

        languageExportCheckBox.setText(Dictionary.get("settings.user.export"));
        languageExportCheckBox.setSelected(userSettingsAdapter.useDefaultLanguageForExport());
        languageExportCheckBox.setOnAction(e -> {
            var selected = languageExportCheckBox.isSelected();
            languageExportCheckBox.setSelected(selected);
            userSettingsAdapter.setUseDefaultLanguageForExport(selected);
        });

        settingsWarningCheckbox.setText(Dictionary.get("settings.user.warning"));
        settingsWarningCheckbox.setSelected(userSettingsAdapter.showWarnings());
        settingsWarningCheckbox.setOnAction(e -> {
            var selected = settingsWarningCheckbox.isSelected();
            settingsWarningCheckbox.setSelected(selected);
            userSettingsAdapter.setShowWarnings(selected);
        });

        //var languageMap = TODO: language
        List.of("en").forEach(l -> languageBox.getItems().add(new SettingsLanguageEntry("en","english")));
        languageBox.setCellFactory(cb -> new SettingsLanguageCell());
        languageBox.setButtonCell(new SettingsLanguageCell());
        languageBox.getSelectionModel().select(new SettingsLanguageEntry("en","english"));
        languageBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                var cell = newValue.getCode();
                userSettingsAdapter.setLanguage(cell);
                //TODO: dispatch update language Event
            }
        });

        addKeybinds();

        Platform.runLater(() -> {
            var stage = (Stage) settingsView.getScene().getWindow();
            stage.setOnCloseRequest(e -> close());
        });
    }

    @Override
    public void addKeybinds() {
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::close);

        settingsView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);
    }

    @Override
    public void close() {
        var stage = (Stage) settingsView.getScene().getWindow();
        stage.close();
    }
}
