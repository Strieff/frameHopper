package com.FrameHopper.app.View.FXViews.Export;

import com.FrameHopper.app.Model.Tag;
import com.FrameHopper.app.Model.Video;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Pair;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Getter
public class ExportActionEntry {
    private final String labelName;
    private final BooleanProperty selected = new SimpleBooleanProperty(true);

    public ExportActionEntry(String labelName) {
        this.labelName = labelName;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public static class VideoExportActionEntry extends ExportActionEntry {
        private final Function<Video, Number> videoActionFunction;
        @Getter
        private final String summaryLabelName;

        public VideoExportActionEntry(
                String labelName,
                Function<Video, Number> videoActionFunction,
                String summaryLabelName
        ) {
            super(labelName);
            this.videoActionFunction = videoActionFunction;
            this.summaryLabelName = summaryLabelName;
        }

        public Number apply(Video v) {
            return videoActionFunction.apply(v);
        }
    }

    public static class TagExportActionEntry extends ExportActionEntry {
        private final Function<Pair<Tag, List<Video>>, Number> tagActionFunction;

        public TagExportActionEntry(
                String labelName,
                Function<Pair<Tag, List<Video>>, Number> tagActionFunction
        ) {
            super(labelName);
            this.tagActionFunction = tagActionFunction;
        }

        public Number apply(Tag t, List<Video> list) {
            return tagActionFunction.apply(new Pair<>(t, list));
        }
    }
}
