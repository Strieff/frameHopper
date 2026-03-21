package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.View.Elements.Language.LanguageEntry;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class SettingsLanguageCell extends ListCell<SettingsLanguageEntry> {
    private final HBox hbox = new HBox(5);
    private final ImageView imageView = new ImageView();
    private final Label label = new Label();

    public SettingsLanguageCell() {
        hbox.getChildren().addAll(imageView, label);
        label.setFont(Font.font("Comic Sans",16));
        label.setTextFill(Color.BLACK);
    }

    @Override
    protected void updateItem(SettingsLanguageEntry language, boolean empty) {
        super.updateItem(language, empty);

        if (empty || language == null) {
            setGraphic(null);
        } else {
            label.setText(language.getLanguageName());

            if (language.getFlagIcon() != null) {
                imageView.setImage(language.getFlagIcon());
                imageView.setFitHeight(16); // Set desired icon size
                imageView.setFitWidth(24);
            } else {
                imageView.setImage(null); // No image if flag is missing
            }

            setGraphic(hbox);
        }
    }
}
