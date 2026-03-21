package com.FrameHopper.app.ui.ve;

import com.FrameHopper.app.boundry.dto.analytics.VideoDataDTO;
import com.FrameHopper.app.ui.language.I18n;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ChartsActionEntry {
    private final Function<List<VideoDataDTO>, Map<String, Number>> chartsActionFunction;
    private final StringProperty labelProperty = new SimpleStringProperty();

    public ChartsActionEntry(String labelName, Function<List<VideoDataDTO>, Map<String, Number>> chartsActionFunction) {
        this.chartsActionFunction = chartsActionFunction;

       labelProperty.bind(
               Bindings.createStringBinding(
                       () -> I18n.tr(labelName),
                       I18n.localeProperty()
               )
       );
    }

    public Map<String, Number> apply(List<VideoDataDTO> data){
        return chartsActionFunction.apply(data);
    }

    public String getLabel() {
        return labelProperty.get();
    }

    public StringProperty labelProperty() {
        return labelProperty;
    }
}
