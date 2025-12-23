package com.example.engineer.View.Elements.DataManagers;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class OpenViewsInformationContainer {
    private Boolean tagManager = false;
    private Boolean settings = false;
    private Boolean export = false;
    private Boolean videoList = false;
    private Boolean createTag = false;
    private Boolean charts = false;

    // Tag Manager
    public void openTagManager(){
        tagManager = true;
    }

    public void closeTagManager(){
        tagManager = false;
    }

    // Settings
    public void openSettings(){
        settings = true;
    }

    public void closeSettings(){
        settings = false;
    }

    // Export
    public void openExport(){
        export = true;
    }

    public void closeExport(){
        export = false;
    }

    // Video List
    public void openVideoList(){
        videoList = true;
    }

    public void closeVideoList(){
        videoList = false;
    }

    // Create Tag
    public void openCreateTag(){
        createTag = true;
    }

    public void closeCreateTag(){
        createTag = false;
    }

    // Charts
    public void openCharts(){
        charts = true;
    }

    public void closeCharts(){
        charts = false;
    }
}

