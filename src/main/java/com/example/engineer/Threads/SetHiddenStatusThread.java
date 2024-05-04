package com.example.engineer.Threads;

import com.example.engineer.Model.Tag;
import com.example.engineer.Service.TagService;
import jakarta.transaction.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SetHiddenStatusThread {
    TagService tagService;
    int id;
    boolean hide;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SetHiddenStatusThread(TagService tagService, int id, boolean hide) {
        this.tagService = tagService;
        this.id = id;
        this.hide = hide;
    }

    public void start(){
        executorService.execute(() -> {
            if(hide)
                tagService.hideTag(id);
            else
                tagService.unHideTag(id);

            executorService.shutdown();

            try {
                if(!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)){
                    executorService.shutdownNow();
                }
            }catch (Exception e){
                executorService.shutdownNow();
            }
        });
    }
}
