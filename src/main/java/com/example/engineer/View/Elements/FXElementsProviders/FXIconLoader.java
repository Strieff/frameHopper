package com.example.engineer.View.Elements.FXElementsProviders;

import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

public class FXIconLoader {
    private final static String FLAG_PATH = "settings"+File.separator+"flags"+File.separator;

    //return 32x32 pixel icon
    public static javafx.scene.image.Image getLargeIcon(String name){
        URL iconURL = FXIconLoader.class.getClassLoader().getResource("icons/"+name);
        return SwingFXUtils.toFXImage(getIcon(iconURL,32),null);
    }

    public static javafx.scene.image.Image getSuperLargeIcon(String name){
        URL iconURL = FXIconLoader.class.getClassLoader().getResource("icons/"+name);
        return SwingFXUtils.toFXImage(getIcon(iconURL,64),null);
    }

    //return 16x16 pixel icon
    public static javafx.scene.image.Image getSmallIcon(String name){
        URL iconURL = FXIconLoader.class.getClassLoader().getResource("icons/"+name);
        return SwingFXUtils.toFXImage(getIcon(iconURL,16),null);
    }

    //return 32x32 flag icon
    public static javafx.scene.image.Image getFlagIcon(String name){
        try {
            URI iconURI = Paths.get(FLAG_PATH + name).toUri();
            return SwingFXUtils.toFXImage(getIcon(iconURI.toURL(), 32),null);
        } catch (MalformedURLException e) {
            return SwingFXUtils.toFXImage(getIcon(null,32),null);
        }
    }

    //return icon in given path of given size
    private static BufferedImage getIcon(URL path, int size){
        if(path != null){
            try{
                var ogImage = ImageIO.read(path);
                var scaledImage = ogImage.getScaledInstance(size,size,Image.SCALE_SMOOTH);
                var bufferedImage = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
                bufferedImage.getGraphics().drawImage(scaledImage,0,0,null);
                return bufferedImage;
            }catch (Exception e){
                return new BufferedImage(0,0,0);
            }
        }
        return new BufferedImage(0,0,0);
    }
}
