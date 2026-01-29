package com.FrameHopper.app.View.FXViews.Charts;

import com.FrameHopper.app.Model.Video;
import com.FrameHopper.app.View.Elements.Language.Dictionary;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record ChartsActionEntry(String labelName, Function<List<Video>, Map<Video, Number>> chartsActionFunction) {
    public Map<Video, Number> apply(List<Video> data) {
        return chartsActionFunction.apply(data);
    }

    public String getLabel() {
        return Dictionary.get(labelName);
    }
}
