package com.example.engineer.View.Elements.Language;

import com.example.engineer.Model.UserSettings;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.List;


@Component(value = "LanguageManager")
@DependsOn("SetDictionary")
public class LanguageManager implements ApplicationContextAware {
    private static ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;
    }

    private final List<LanguageChangeListener> listeners = new ArrayList<>();

    //register event listeners
    public void addListener(LanguageChangeListener listener) {
        listeners.add(listener);
    }

    //method to fire event
    private void fireLanguageChangeEvent(){
        for(LanguageChangeListener listener : listeners)
            listener.changeLanguage();
    }

    //changes language
    public void changeLanguage(String code) {
        try {
            Dictionary.setDictionary(code);
            fireLanguageChangeEvent();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //returns a language list in given language
    public List<String> getLanguageList() {
        String code = UserSettings.getInstance().getLanguage();
        Locale locale = Locale.of(code);
        return Dictionary.getAvailableLanguages().stream()
                .map(l -> {
                    Locale otherLocale = Locale.of(l);
                    return otherLocale.getDisplayLanguage(locale);
                }).toList();
    }

    //returns a map of language associated with language code
    public Map<String,String> getLanguageMap() {
        Set<String> codeList = Dictionary.getAvailableLanguages();
        List<String> nameList = getLanguageList();

        Map<String,String> langMap = new HashMap<>();

        Iterator<String> codeIterator = codeList.iterator();
        Iterator<String> nameIterator = nameList.iterator();

        while (codeIterator.hasNext() && nameIterator.hasNext())
            langMap.put(codeIterator.next(), nameIterator.next());

        return langMap;
    }

    //returns a language name in given language
    public static String getLanguageName(String code) {
        return ctx.getBean(LanguageManager.class).getLanguageMap().get(code);
    }
}
