package com.example.engineer.View.Elements;

import com.example.engineer.View.Elements.Language.Dictionary;

import javax.swing.*;
import java.awt.*;

public class DialogProvider {
    //return true if YES and false if NO
    public static boolean yesNoDialog(String message){
            Object[] yesNoOptions = {
                    Dictionary.get("option.yes"),
                    Dictionary.get("option.no")
            };

            return JOptionPane.showOptionDialog(
                    null,
                    message,
                    "",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    yesNoOptions,
                    yesNoOptions[1]
            ) == 0;
    }

    //return the index of chosen option,
    public static int customDialog(String message,int defaultOption,String... options){
        return JOptionPane.showOptionDialog(
                null,
                message,
                "",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[defaultOption]
        );
    }

    //show error dialog
    public static void errorDialog(String message, String title, Component ref){
        JOptionPane.showMessageDialog(
                ref,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    //show error dialog
    public static void errorDialog(String message){
        errorDialog(message,"",null);
    }

    //show error dialog
    public static void errorDialog(String message, String title){
        errorDialog(message,title,null);
    }

    //show error dialog
    public static void errorDialog(String message, Component ref){
        errorDialog(message,"",ref);
    }

    //show message dialog
    public static void messageDialog(String message, String title, Component ref){
        JOptionPane.showMessageDialog(
                ref,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    //show message dialog
    public static void messageDialog(String message){
        messageDialog(message,"",null);
    }

    //show message dialog
    public static void messageDialog(String message, String title){
        messageDialog(message,title,null);
    }

    //show message dialog
    public static void messageDialog(String message, Component ref){
        messageDialog(message,"",ref);
    }
}
