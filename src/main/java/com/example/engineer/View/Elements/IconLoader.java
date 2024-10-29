package com.example.engineer.View.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

public class IconLoader {
    private final static String FLAG_PATH = "settings"+File.separator+"flags"+File.separator;

    //return 32x32 pixel icon
    public static Icon getLargeIcon(String name){
        URL iconURL = IconLoader.class.getClassLoader().getResource("icons/"+name);
        return new ImageIcon(getIcon(iconURL,32));
    }

    public static Icon getSuperLargeIcon(String name){
        URL iconURL = IconLoader.class.getClassLoader().getResource("icons/"+name);
        return new ImageIcon(getIcon(iconURL,64));
    }

    //return 16x16 pixel icon
    public static Icon getSmallIcon(String name){
        URL iconURL = IconLoader.class.getClassLoader().getResource("icons/"+name);
        return new ImageIcon(getIcon(iconURL,16));
    }

    //return 32x32 flag icon
    public static ImageIcon getFlagIcon(String name){
        try {
            URI iconURI = Paths.get(FLAG_PATH + name).toUri();
            return new ImageIcon(getIcon(iconURI.toURL(), 32));
        } catch (MalformedURLException e) {
            return new ImageIcon(getIcon(null,32));
        }
    }

    //return icon in given path of given size
    private static Image getIcon(URL path, int size){
        Image scaledIcon;
        if (path != null) {
            ImageIcon imageIcon = new ImageIcon(path);
            scaledIcon = imageIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return scaledIcon;
        }

        return new BufferedImage(0,0,0);
    }
}
