package com.example.engineer.DBActions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DBAction {
    protected final ExecutorService executor;

    protected DBAction() {
        this.executor = Executors.newCachedThreadPool();
    }

    public void run(){

    }

    protected void shutdown(){
        executor.shutdown();

        try {
            if(!executor.awaitTermination(800, TimeUnit.MILLISECONDS)){
                executor.shutdownNow();
            }
        }catch (Exception e){
            executor.shutdownNow();
        }
    }
}
