package com.example.engineer.FrameProcessor;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class FrameProcessorRequestManager{
    @Autowired
    private FrameProcessorClient client;

    @Getter
    private String videoFolderPath;

    public void closeServer(){
        client.send("-1;0;0",false);
    }

    public String setCachePath(){
        return client.send("0;0;"+ Paths.get("cache").toAbsolutePath(),true);
    }

    public String loadFirsSet(String path){
        videoFolderPath = client.send("1;0;"+path,true);
        videoFolderPath = videoFolderPath.substring(videoFolderPath.lastIndexOf('\\'));

        return videoFolderPath;
    }

    public void loadNthSet(int set,boolean wait){
        client.send("3;"+set+";0",wait);
    }

    public String getVideoData(){
        return client.send("2;0;0",true);
    }

    public String getVideoData(String path){
        return client.send("4;0;"+path,true);
    }
}
