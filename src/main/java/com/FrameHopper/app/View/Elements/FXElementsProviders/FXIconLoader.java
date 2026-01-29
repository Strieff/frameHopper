package com.FrameHopper.app.View.Elements.FXElementsProviders;

import javafx.scene.image.Image;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

public class FXIconLoader {
    private final static String FLAG_PATH = "settings"+File.separator+"flags"+File.separator;

    //return 32x32 pixel icon
    public static Image getLargeIcon(String name){
        URL iconURL = FXIconLoader.class.getClassLoader().getResource("icons/"+name);
        return getIcon(iconURL,32);
    }

    public static Image getSuperLargeIcon(String name){
        URL iconURL = FXIconLoader.class.getClassLoader().getResource("icons/"+name);
        return getIcon(iconURL,64);
    }

    //return 16x16 pixel icon
    public static Image getSmallIcon(String name){
        URL iconURL = FXIconLoader.class.getClassLoader().getResource("icons/"+name);
        return getIcon(iconURL,16);
    }

    //return 32x32 flag icon
    public static Image getFlagIcon(String name){
        try {
            URI iconURI = Paths.get(FLAG_PATH + name).toUri();
            return getIcon(iconURI.toURL(), 32);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    //return icon in given path of given size
    private static Image getIcon(URL path, int size){
        if(path == null) return null;

        return new javafx.scene.image.Image(
                path.toExternalForm(),
                size,
                size,
                true,
                true,
                false
        );
    }


}
