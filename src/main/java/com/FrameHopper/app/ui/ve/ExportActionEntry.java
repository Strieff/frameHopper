package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.boundry.dto.TagDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataAnalyticsDTO;
import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

public class ExportActionEntry {
    @Getter
    private final String labelName;
    private final BooleanProperty selected;

    public ExportActionEntry(String labelName) {
        this.labelName = labelName;
        this.selected = new SimpleBooleanProperty(false);
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public String getLabel() {
        return Dictionary.get(labelName);
    }

    public static class VideoExportActionEntry extends ExportActionEntry {
        private final Function<VideoDataDTO, Number> videoActionFunction;
        private final Function<VideoDataAnalyticsDTO, Number> videoSummaryFunction;
        @Getter
        private final String summaryLabelName;

        public VideoExportActionEntry(
                String labelName,
                String summaryLabelName,
                Function<VideoDataDTO, Number> videoActionFunction,
                Function<VideoDataAnalyticsDTO, Number> videoSummaryFunction
        ) {
            super(labelName);
            this.videoActionFunction = videoActionFunction;
            this.summaryLabelName = summaryLabelName;
            this.videoSummaryFunction = videoSummaryFunction;
        }

        public Number apply(VideoDataDTO data) {
            return videoActionFunction.apply(data);
        }
        public Number summary(VideoDataAnalyticsDTO analytics) { return videoSummaryFunction.apply(analytics); }

        public String getSummaryLabel() {
            return Dictionary.get(summaryLabelName);
        }

        public boolean hasSummaryAction() {
            return videoSummaryFunction != null;
        }
    }

    public static class TagExportActionEntry extends ExportActionEntry {
        private final Function<TagExportDto, Number> tagActionFunction;

        public TagExportActionEntry(
                String labelName,
                Function<TagExportDto, Number> tagActionFunction
        ) {
            super(labelName);
            this.tagActionFunction = tagActionFunction;
        }

        public Number apply(TagExportDto tagExportDto) {
            return tagActionFunction.apply(tagExportDto);
        }

        public record TagExportDto(TagDTO tag, List<VideoDataDTO> videos) {}
    }
}
