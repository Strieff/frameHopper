package com.example.engineer.View.Elements.FXElementsProviders;

import java.io.File;

public class RestartResolver {
    public static void reset(){
        try {
            String jarDir = new File(RestartResolver.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            File jarFile = new File(jarDir, "StartFrameHopper.jar");

            if(!jarFile.exists()) {
                FXDialogProvider.messageDialog("PLEASE RESTART");
                ViablePathProvider.clearFiles();
                System.exit(0);
            }

            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath());
            processBuilder.inheritIO();
            processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ViablePathProvider.clearFiles();
        System.exit(0);
    }
}
