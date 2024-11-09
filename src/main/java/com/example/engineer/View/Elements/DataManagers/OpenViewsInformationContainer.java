package com.example.engineer.View.Elements.DataManagers;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class OpenViewsInformationContainer {
    @Getter
    private Boolean tagManager = false;
    @Getter
    private Boolean settings = false;
    @Getter
    private Boolean export = false;
    @Getter
    private Boolean videoList = false;
    @Getter
    private Boolean createTag = false;

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
}

