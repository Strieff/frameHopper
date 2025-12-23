package com.example.engineer.View.FXViews.VideoMerge;

import com.example.engineer.Model.Video;
import com.example.engineer.View.Elements.DataManagers.OpenViewsInformationContainer;
import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;
import com.example.engineer.View.Elements.FXElementsProviders.FXMLViewLoader;
import com.example.engineer.View.Elements.FXElementsProviders.RestartResolver;
import com.example.engineer.View.Elements.Language.Dictionary;
import com.example.engineer.View.Elements.Language.LanguageChangeListener;
import com.example.engineer.View.Elements.Language.LanguageManager;
import com.example.engineer.View.FXViews.MainView.MainViewService;
import com.example.engineer.View.FXViews.VideoManagementList.VideoManagementListController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Scope("prototype")
public class VideoMetadataMergeController implements LanguageChangeListener {
    @FXML
    private BorderPane metadataComparisonView;
    @FXML
    private Label titleLabel,oldDataLabel,newDataLabel,conflictLabel;
    @FXML
    private Button cancelButton,continueButton;

    @Autowired
    private VideoMergeService viewService;
    @Autowired
    private MainViewService mainViewService;
    @Autowired
    private OpenViewsInformationContainer openViews;

    private Video oldVideo;
    private Video newVideo;

    private VideoManagementListController parent;

    @FXML
    public void initialize() {
        LanguageManager.register(this);

        titleLabel.setText(Dictionary.get("mv.metadata"));

        cancelButton.setText(Dictionary.get("cancel"));
        cancelButton.setOnAction(event -> closeWindow());
        continueButton.setText(Dictionary.get("continue"));
        continueButton.setOnAction(event -> {
            if(conflictLabel.getText().equals(Dictionary.get("mv.metadata.no-conflict"))) {
                if (viewService.getMergeConfirmation())
                    if (!viewService.doBothHaveData(oldVideo, newVideo)) {
                        merge();
                        parent.close();
                    }
            }else
                openFrameData();
        });


        oldDataLabel.setWrapText(true);
        newDataLabel.setWrapText(true);

        Platform.runLater(() -> {
            var stage = (Stage) metadataComparisonView.getScene().getWindow();
            stage.setOnCloseRequest(e -> LanguageManager.unregister(this));
        });
    }

    public void init(int id, File newFile, VideoManagementListController parent) throws IOException, InterruptedException {
        oldVideo = viewService.getVideo(id);
        newVideo = viewService.getVideoFromFile(newFile);
        this.parent = parent;

        oldDataLabel.setText(viewService.getVideoData(oldVideo,true));
        newDataLabel.setText(viewService.getVideoData(newVideo,false));

        var conflictData = viewService.compareMetadata(oldVideo, newVideo);
        if(conflictData.isEmpty())
            conflictLabel.setText(Dictionary.get("mv.metadata.no-conflict"));
        else {
            conflictLabel.setText(String.format(Dictionary.get("mv.metadata.conflict"), conflictData));
            conflictLabel.setTextFill(Color.RED);
        }
    }

    private void openFrameData(){
        try {
            var loader = FXMLViewLoader.getView("FrameComparisonViewModel");

            Parent root = loader.load();
            var metadataComparisonScene = new Scene(root);

            FrameDataMergeController controller = loader.getController();
            controller.init(oldVideo,newVideo,parent);

            var secondaryStage = new Stage();
            secondaryStage.setScene(metadataComparisonScene);
            secondaryStage.setTitle("Path Change");

            secondaryStage.initOwner(metadataComparisonView.getScene().getWindow());
            secondaryStage.show();
        } catch (Exception e) {
            FXDialogProvider.errorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    private void merge(){
        viewService.mergeData(oldVideo,newVideo);
        if(mainViewService.getCurrentId() == newVideo.getId())
            RestartResolver.reset();

    }

    public void closeWindow() {
        LanguageManager.unregister(this);
        Stage stage = (Stage) metadataComparisonView.getScene().getWindow();
        stage.close();
    }

    @Override
    public void changeLanguage() {
        titleLabel.setText(Dictionary.get("mv.metadata"));

        oldDataLabel.setText(viewService.getVideoData(oldVideo,true));
        newDataLabel.setText(viewService.getVideoData(newVideo,false));


        var conflictData = viewService.compareMetadata(oldVideo, newVideo);
        if(conflictData.isEmpty())
            conflictLabel.setText(Dictionary.get("mv.metadata.no-conflict"));
        else {
            conflictLabel.setText(String.format(Dictionary.get("mv.metadata.conflict"), conflictData));
            conflictLabel.setTextFill(Color.RED);
        }

        cancelButton.setText(Dictionary.get("cancel"));
        continueButton.setText(Dictionary.get("continue"));
    }
}
