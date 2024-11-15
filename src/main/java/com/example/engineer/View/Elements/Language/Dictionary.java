package com.example.engineer.View.Elements.Language;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Dictionary {
    private static final String SETTINGS_PATH = "settings/";

    private static Dictionary instance;

    private final Map<String,Properties> dictionaries = new HashMap<>();
    private static Properties dictionary = new Properties();

    public Dictionary(String langCode) {
        instance = this;

        Path resourcePath = Path.of(SETTINGS_PATH);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(resourcePath,"lang_*.properties")){
            for (Path path : stream){
                String fileName = path.getFileName().toString();
                if(fileName.startsWith("lang_")){
                    String lang = fileName.replace("lang_","").replace(".properties","");

                    Properties properties = new Properties();
                    try (InputStream inputStream = Files.newInputStream(path)) {
                        properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    }

                    dictionaries.put(lang,properties);
                }
            }

            if(dictionaries.isEmpty()) throw new RuntimeException("No language files. Defaulting to English!");

            if(!dictionaries.containsKey(langCode)) throw new RuntimeException("No such language: " + langCode + " found. Defaulting to English!");

            setDictionary(langCode);
        }catch (Exception e){
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("lang.properties")) {
                Properties properties = new Properties();
                properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                dictionaries.put("en",properties);
                setDictionary("en");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
        }
    }

    public static void setDictionary(String language) throws RuntimeException {
        if(!instance.dictionaries.containsKey(language)) throw new RuntimeException("No such language: " + language + " found. Cannot change language!");

        dictionary = instance.dictionaries.get(language);
    }

    public static String get(String name){
        return dictionary.containsKey(name)
                ? dictionary.getProperty(name)
                : instance.dictionaries.get("en").getProperty(name);
    }

    public static String get(String language, String name){
        return instance.dictionaries.get(language).containsKey(name)
                ? instance.dictionaries.get(language).getProperty(name)
                : instance.dictionaries.get("en").getProperty(name);
    }

    public static Set<String> getAvailableLanguages(){
        return instance.dictionaries.keySet();
    }
}
