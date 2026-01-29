package com.FrameHopper.app.View.FXViews.Settings;

import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.OpenViewsInformationContainer;
import com.FrameHopper.app.settings.UserSettingsService;
import com.FrameHopper.app.View.Elements.DataManagers.ViewContainer.ViewFlag;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider;
import com.FrameHopper.app.View.Elements.FXElementsProviders.FXMLViewLoader;
import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.View.Elements.Language.LanguageChangeListener;
import com.FrameHopper.app.View.Elements.Language.LanguageEntry;
import com.FrameHopper.app.View.Elements.Language.LanguageManager;
import com.FrameHopper.app.View.FXViews.VideoDetails.VideoManagementDetailsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class SettingsController implements LanguageChangeListener {
    @FXML
    private BorderPane settingsView;
    @FXML
    private CheckBox showHiddenTagsCheckBox,openRecentCheckBox,languageExportCheckBox,settingsWarningCheckbox;
    @FXML
    private ComboBox<LanguageEntry> languageBox;
    @FXML
    private Button manageButton;

    private final SettingsService viewService;
    private final OpenViewsInformationContainer viewContainer;
    private final UserSettingsService userSettingsService;

    private final Map<KeyCombination,Runnable> keyActions = new HashMap<>();

    public SettingsController(
            SettingsService viewService,
            OpenViewsInformationContainer viewContainer,
            UserSettingsService userSettingsService
    ) {
        this.viewService = viewService;
        this.viewContainer = viewContainer;
        this.userSettingsService = userSettingsService;

        LanguageManager.register(this);
    }


    public void initialize() {
        showHiddenTagsCheckBox.setSelected(userSettingsService.ShowHidden());
        showHiddenTagsCheckBox.setText(Dictionary.get("settings.user.hidden"));
        showHiddenTagsCheckBox.setOnMouseClicked(event -> handleShowHiddenTags());

        openRecentCheckBox.setSelected(userSettingsService.openRecent());
        openRecentCheckBox.setText(Dictionary.get("settings.user.recent"));
        openRecentCheckBox.setOnMouseClicked(event -> handleOpenRecent());

        languageExportCheckBox.setSelected(userSettingsService.useDefaultLanguage());
        languageExportCheckBox.setText(Dictionary.get("settings.user.export"));
        languageExportCheckBox.setOnMouseClicked(event -> handleChosenLanguage());

        settingsWarningCheckbox.setSelected(userSettingsService.showSettingsWarning());
        settingsWarningCheckbox.setText(Dictionary.get("settings.user.warning"));
        settingsWarningCheckbox.setOnMouseClicked(event -> handleSettingsWarnings());

        //language box
        languageBox.getItems().addAll(viewService.getLanguages());
        languageBox.setCellFactory(cb -> new LanguageCell());
        languageBox.setButtonCell(new LanguageCell());
        languageBox.getSelectionModel().select(viewService.getCurrentLanguage());
        languageBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                var languageCode = newValue.getCode();
                userSettingsService.setLanguage(languageCode);
            }
        });

        manageButton.setText(Dictionary.get("settings.button.list"));

        //kye binds
        keyActions.put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN), this::onShiftSPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN), this::onShiftDPressed);
        keyActions.put(new KeyCodeCombination(KeyCode.L, KeyCombination.SHIFT_DOWN), this::onShiftLPressed);

        //add key binds
        settingsView.addEventFilter(KeyEvent.KEY_PRESSED,this::handleKeyPressed);

        Platform.runLater(() -> {
            var stage = (Stage) settingsView.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                LanguageManager.unregister(this);
                viewContainer.close(ViewFlag.SETTINGS);
            });
        });
    }

    private void handleKeyPressed(KeyEvent event){
        keyActions.keySet().stream()
                .filter(k -> k.match(event))
                .findFirst()
                .ifPresent(k -> keyActions.get(k).run());
    }

    //CLOSE WINDOW
    private void onShiftSPressed() {
        LanguageManager.unregister(this);
        var stage = (Stage) settingsView.getScene().getWindow();
        stage.close();
        viewContainer.close(ViewFlag.SETTINGS);
    }

    //OPEN VIDEO MANAGEMENT LIST
    @FXML
    protected void onManage(){
        openVideoManagement();
    }

    private void onShiftLPressed(){
        openVideoManagement();
    }

    private void openVideoManagement(){
        if(viewContainer.isClosed(ViewFlag.VIDEO_LIST)) {
            FXMLViewLoader.getView(
                    "VideoManagementListViewModel",
                    "Video Management",
                    settingsView
            );
            viewContainer.open(ViewFlag.VIDEO_LIST);
        }
        else
            FXDialogProvider.errorDialog(Dictionary.get("open.video-list"));
    }

    //OPEN CURRENT VIDEO DETAILS
    private void onShiftDPressed() {
        try{
            var loader = FXMLViewLoader.getView(
                    "VideoManagementDetailsViewModel",
                    "Video Details",
                    settingsView
            );

            //get controller
            VideoManagementDetailsController videoController = loader.getController();
            var vid = viewService.getCurrentVideo();
            if(vid!=null)
                videoController.init(vid,null);
            else
                throw new Exception("No video is open");
        }catch (Exception e){
            e.printStackTrace();
            FXDialogProvider.errorDialog(e.getMessage());
        }
    }

    //HANDLE SHOW HIDDEN TAGS
    private void handleShowHiddenTags(){
        viewService.changeShowHidden(showHiddenTagsCheckBox.isSelected());
    }

    //HANDLE OPEN RECENT
    private void handleOpenRecent(){
        userSettingsService.setOpenRecent(openRecentCheckBox.isSelected());
    }

    //HANDLE CHOSEN LANGUAGE
    private void handleChosenLanguage(){
        userSettingsService.setUseDefaultLanguage(languageExportCheckBox.isSelected());
    }

    //HANDLE SETTINGS WARNINGS
    private void handleSettingsWarnings(){
        userSettingsService.setSettingsWarnings(settingsWarningCheckbox.isSelected());
    }

    @Override
    public void changeLanguage() {
        //change language box
        for(var e : languageBox.getItems())
            e.setLanguage();

        //settings
        showHiddenTagsCheckBox.setText(Dictionary.get("settings.user.hidden"));
        openRecentCheckBox.setText(Dictionary.get("settings.user.recent"));
        languageExportCheckBox.setText(Dictionary.get("settings.user.export"));
        settingsWarningCheckbox.setText(Dictionary.get("settings.user.warning"));

        //buttons
        manageButton.setText(Dictionary.get("settings.button.list"));
    }
}

