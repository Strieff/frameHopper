package com.FrameHopper.app.View.FXViews.Notes;

import com.FrameHopper.app.Model.Video;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

public class TableEntry {
    @Getter
    private final Video video;

    private final SimpleStringProperty name;
    private final SimpleIntegerProperty notesCount;

    public TableEntry(Video video) {
        this.video = video;
        name = new SimpleStringProperty(video.getName());
        notesCount = new SimpleIntegerProperty(video.getComments() != null ? video.getComments().size() : 0);
    }

    public String getName()  { return name.get(); }

    public SimpleStringProperty nameProperty() { return name; }

    public int getNotesCount() { return notesCount.get(); }

    public SimpleIntegerProperty notesCountProperty() { return notesCount; }

    public void updateNotesCount() {
        notesCount.set(
                video.getComments() != null ? video.getComments().size() : 0
        );
    }
}
