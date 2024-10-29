package com.example.engineer.View.Elements;

public class ProgramResetResolver {
    public static void reset(){
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd","/c","restartProgram");
            pb.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.exit(0);
    }
}
