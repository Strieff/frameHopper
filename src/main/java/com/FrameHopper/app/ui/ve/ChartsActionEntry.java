package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.View.Elements.Language.Dictionary;
import com.FrameHopper.app.boundry.dto.VideoDTO;
import com.FrameHopper.app.boundry.dto.VideoDataDTO;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record ChartsActionEntry(
        String labelName,
        Function<List<VideoDataDTO>, Map<String, Number>> chartsActionFunction
) {
    public Map<String, Number> apply(List<VideoDataDTO> data){
        return chartsActionFunction.apply(data);
    }

    public String getLabel() {
        return Dictionary.get(labelName);
    }
}
