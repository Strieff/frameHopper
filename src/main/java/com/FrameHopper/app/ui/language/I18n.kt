package com.FrameHopper.app.ui.language

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import java.net.URLClassLoader
import java.nio.file.Path
import java.text.MessageFormat
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

object I18n {
    private val externalI18nDir: Path = Path.of("i18n")
    private const val EXTERNAL_BASE_NAME = "translations"
    private const val INTERNAL_BASE_NAME = "i18n.translations"
    var defaultLocale: Locale = Locale.ENGLISH

    private val locale: ObjectProperty<Locale> = SimpleObjectProperty(defaultLocale)
    private val cache = mutableMapOf<Locale, ResourceBundle?>()

    @JvmStatic
    fun localeProperty(): ObjectProperty<Locale> = locale

    @JvmStatic
    fun getLocale(): Locale = locale.get()

    @JvmStatic
    fun setLocale(code: String) {
        locale.set(Locale.of(code))
    }

    @JvmStatic
    fun bind(key: String, vararg args: Any?): StringBinding {
        return Bindings.createStringBinding(
            {tr(key,*args)},
            locale
        )
    }

    @JvmStatic
    fun tr(key: String,vararg args: Any?): String {
        val pattern = lookup(key)
        return if (args.isEmpty()) {
            pattern
        } else {
            MessageFormat.format(pattern, *args)
        }
    }

    private fun lookup(key: String): String {
        val bundle = getBundle(getLocale())

        if(bundle != null) {
            try {
                return bundle.getString(key)
            } catch (_: MissingResourceException) {}
        }

        val fallback = getInternalBundle()
        return try {
            fallback.getString(key)
        } catch (_: MissingResourceException) {
            "!$key!"
        }
    }

    private fun getBundle(locale: Locale): ResourceBundle? {
        return cache.getOrPut(locale) {
            loadExternalThenInternal(locale)
        }
    }

    private fun loadExternalThenInternal(locale: Locale): ResourceBundle {
        val external = getExternalBundle(locale)
        if (external != null) return external

        return getInternalBundle()
    }

    private fun getExternalBundle(locale: Locale): ResourceBundle? {
        return try {
            val urls = arrayOf(externalI18nDir.toUri().toURL())
            val loader = URLClassLoader(urls, I18n::class.java.classLoader)
            ResourceBundle.getBundle(EXTERNAL_BASE_NAME, locale, loader)
        } catch (_: Exception) {
            null
        }
    }

    private fun getInternalBundle(): ResourceBundle {
        return ResourceBundle.getBundle(INTERNAL_BASE_NAME, Locale.ENGLISH)
    }

    @JvmStatic
    fun clearCache() {
        cache.clear()
        locale.set(locale.get())
    }
}