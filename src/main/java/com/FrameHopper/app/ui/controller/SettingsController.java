package com.FrameHopper.app.ui.controller;

import com.FrameHopper.app.adapters.settings.UserSettingsAdapter;
import com.FrameHopper.app.ui.UIFlag;
import com.FrameHopper.app.ui.UIManager;
import com.FrameHopper.app.ui.UiView;
import com.FrameHopper.app.ui.eventing.NewLanguageListener;
import com.FrameHopper.app.ui.language.I18n;
import com.FrameHopper.app.ui.utils.AvailableLanguageUtils;
import com.FrameHopper.app.ui.utils.LanguageUtils;
import com.FrameHopper.app.ui.ve.SettingsLanguageCell;
import com.FrameHopper.app.ui.ve.SettingsLanguageEntry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class SettingsController extends UiView implements
        NewLanguageListener
{
    @FXML
    private BorderPane settingsView;
    @FXML
    private CheckBox showHiddenTagsCheckBox, openRecentCheckBox, languageExportCheckBox, settingsWarningCheckbox;
    @FXML
    private ComboBox<SettingsLanguageEntry> languageBox;

    private final UserSettingsAdapter userSettingsAdapter;
    private final UIManager uiManager;

    public SettingsController(
            UserSettingsAdapter userSettingsAdapter,
            UIManager uiManager
    ) {
        this.userSettingsAdapter = userSettingsAdapter;
        this.uiManager = uiManager;
    }

    @FXML
    public void initialize() {
        bind(showHiddenTagsCheckBox, "settings.setting.show-hidden-tags");
        showHiddenTagsCheckBox.setSelected(userSettingsAdapter.showHidden());
        showHiddenTagsCheckBox.setOnAction(e -> {
            var selected = showHiddenTagsCheckBox.isSelected();
            showHiddenTagsCheckBox.setSelected(selected);
            userSettingsAdapter.changeShowHidden(selected);
        });

        bind(openRecentCheckBox, "settings.setting.open-recent");
        openRecentCheckBox.setSelected(userSettingsAdapter.openRecent());
        openRecentCheckBox.setOnAction(e -> {
            var selected = openRecentCheckBox.isSelected();
            openRecentCheckBox.setSelected(selected);
            userSettingsAdapter.setOpenRecent(selected);
        });

        bind(languageExportCheckBox, "settings.setting.use-chosen-language-for-export");
        languageExportCheckBox.setSelected(userSettingsAdapter.useDefaultLanguageForExport());
        languageExportCheckBox.setOnAction(e -> {
            var selected = languageExportCheckBox.isSelected();
            languageExportCheckBox.setSelected(selected);
            userSettingsAdapter.setUseDefaultLanguageForExport(selected);
        });

        bind(settingsWarningCheckbox, "settings.setting.show-settings-warnings");
        settingsWarningCheckbox.setSelected(userSettingsAdapter.showWarnings());
        settingsWarningCheckbox.setOnAction(e -> {
            var selected = settingsWarningCheckbox.isSelected();
            settingsWarningCheckbox.setSelected(selected);
            userSettingsAdapter.setShowWarnings(selected);
        });

        loadLanguages();

        addKeybinds();

        Platform.runLater(() -> {
            var stage = (Stage) settingsView.getScene().getWindow();
            bind(stage, "settings.stage");
            stage.setOnCloseRequest(e -> close());
        });
    }

    private void loadLanguages() {
        var availableLanguagesEntries = AvailableLanguageUtils.getAvailableLanguages()
                .stream().collect(Collectors.toMap(
                        c -> c,
                        LanguageUtils::getFlagIcon
                )).entrySet().stream()
                .map(le -> new SettingsLanguageEntry(le.getKey(), le.getValue()))
                .toList();
        languageBox.getItems().clear();
        languageBox.getItems().addAll(availableLanguagesEntries);
        languageBox.getSelectionModel().select(new SettingsLanguageEntry(userSettingsAdapter.getLanguage(), null));
        languageBox.setCellFactory(cb -> new SettingsLanguageCell());
        languageBox.setButtonCell(new SettingsLanguageCell());
        languageBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                var code = newValue.getCode();
                userSettingsAdapter.setLanguage(code);
                I18n.setLocale(code);
            }
        });
    }

    @Override
    public void addKeybinds() {
        keyActions.put(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN), this::close);

        addEventFilter(settingsView);
    }

    @Override
    public void close() {
        uiManager.close(UIFlag.SETTINGS);
        var stage = (Stage) settingsView.getScene().getWindow();
        stage.close();
    }

    @Async
    @Override
    public void newLanguageCreated() {
        loadLanguages();
    }
}
