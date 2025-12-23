package com.example.engineer.View.Elements.Language;

import com.example.engineer.config.UserSettings;
import com.example.engineer.View.Elements.FXElementsProviders.FXDialogProvider;

import java.io.*;

public class DictionaryCreator {
    public static void create(){
        var languageCode = FXDialogProvider.inputDialog();

        if(languageCode.isBlank()){
            FXDialogProvider.errorDialog("Name cannot be empty");
            return;
        }

        if(LanguageManager.getLanguageName(languageCode) != null){
            FXDialogProvider.errorDialog("Language already exists");
            return;
        }

        var dictionaryPath = DictionaryCreator.class.getClassLoader().getResource("lang.properties").getPath().substring(1).replaceAll("%20", " ");

        try(
                var bw = new BufferedWriter(new FileWriter("settings"+File.separator+"lang_"+languageCode+".properties"));
                var br = new BufferedReader(new FileReader(dictionaryPath))
        ){
            br.lines()
                    .map(l -> {
                        if(!l.startsWith("#")) {
                            var v = l.split("=")[0]+"=";
                            return  v.equals("=") ? v.replace("=", "") : v;
                        }

                        return l;
                    })
                    .forEach(l -> {
                        try {
                            bw.write(l);
                            bw.newLine();
                        }
                        catch (Exception e) {throw new RuntimeException(e);}
                    });
        }catch (Exception e){
            FXDialogProvider.errorDialog(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void reload(){
        new Dictionary(UserSettings.getInstance().getLanguage());
        LanguageManager.fireLanguageChangeEvent();
    }
}
