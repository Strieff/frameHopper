package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.boundry.dto.VideoDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

public class NotesVideoTableEntry {
    @Getter
    private VideoDTO video;

    private final SimpleStringProperty name;
    private final SimpleIntegerProperty notesCount;

    public NotesVideoTableEntry(VideoDTO video) {
        this.video = video;
        name = new SimpleStringProperty(video.name());
        notesCount = new SimpleIntegerProperty(video.comments() != null ? video.comments().size() : 0);
    }

    public void setName(VideoDTO video) {
        this.video = video;
        name.set(video.name());
    }

    public String getName()  { return name.get(); }

    public SimpleStringProperty nameProperty() { return name; }

    public int getNotesCount() { return notesCount.get(); }

    public SimpleIntegerProperty notesCountProperty() { return notesCount; }

    public void updateNotesCount() {
        notesCount.set(
                video.comments() != null ? video.comments().size() : 0
        );
    }
}
