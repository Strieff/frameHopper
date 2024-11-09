package com.example.engineer.View.Elements.FXElementsProviders;

public class FXRestartResolver {
    public static void reset(){
        if(System.getProperty("os.name").toLowerCase().contains("win"))
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd","/c","restartProgram");
                pb.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        else
            FXDialogProvider.messageDialog("PLEASE RESTART");

        System.exit(0);

    }
}
