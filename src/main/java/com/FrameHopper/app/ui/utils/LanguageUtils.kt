package com.FrameHopper.app.ui.utils

import com.FrameHopper.app.View.Elements.FXElementsProviders.FXDialogProvider
import javafx.scene.image.Image
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.Locale
import java.util.Locale.getDefault
import kotlin.streams.asSequence

object LanguageUtils {
    private const val INTERNAL_EN_ICON = "/icons/en.png"
    private val externalFlagIcons: Path = Paths.get("settings/flags")
    private val fallbackIcon: Image by lazy {
        requireNotNull(LanguageUtils::class.java.getResourceAsStream(INTERNAL_EN_ICON)).use {
            Image(it)
        }
    }
    private val flagIcons: MutableMap<String, Image> by lazy {
        loadExternalFlagsIcons().toMutableMap()
    }

    private fun loadExternalFlagsIcons(): Map<String, Image> {
        if(!Files.exists(externalFlagIcons) || !Files.isDirectory(externalFlagIcons))
            return emptyMap()

        return Files.list(externalFlagIcons).use { paths ->
            paths
                .asSequence()
                .filter { Files.isRegularFile(it) }
                .filter { it.fileName.toString().lowercase().endsWith(".png") }
                .mapNotNull { path ->
                    val fileName = path.fileName.toString()
                    val code = fileName.substringBeforeLast(".").lowercase()

                    try {
                        code to Files.newInputStream(path).use { Image(it) }
                    } catch (_: Exception) {
                        null
                    }
                }
                .toMap()
        }
    }

    @JvmStatic
    fun getFlagIcon(countryCode: String?): Image {
        val code = countryCode?.lowercase(getDefault()) ?: return fallbackIcon
        return flagIcons[code] ?: fallbackIcon
    }

    @JvmStatic
    fun getFlagIcon(locale: Locale): Image {
        val code = locale.country.lowercase()
        return if (code.isNotBlank()) flagIcons[code] ?: fallbackIcon else fallbackIcon
    }
}

object AvailableLanguageUtils {
    @JvmStatic
    fun getAvailableLanguages(): List<String> {
        val languages = mutableSetOf<String>()

        val dir = Path.of("i18n")
        if(Files.exists(dir)) {
            Files.list(dir).use { paths ->
                paths
                    .asSequence()
                    .filter { Files.isRegularFile(it) }
                    .map { it.fileName.toString() }
                    .filter { it.startsWith("translations") && it.endsWith(".properties") }
                    .map { it.replace("translations_","").replace(".properties", "")}
                    .forEach { languages.add(it) }
            }
        }

        languages.add("en")
        return languages.sorted().toList()
    }
}

object LanguageBundleUtils {
    @JvmStatic
    fun creteBundle() {
        val lang = FXDialogProvider.inputDialog() ?: return
        //if(!isValidLocale(lang)) //TODO: messsage abt unknown locale

        val languages = AvailableLanguageUtils.getAvailableLanguages()
        if(languages.contains(lang.lowercase())) return //TODO: error

        val stream = ClassLoader.getSystemClassLoader()
            .getResourceAsStream("translations_template.properties")
            ?: throw Exception("Template not found!")

        val dir = Path.of("i18n")

        val fileName = "translations_$lang.properties"

        val target = dir.resolve(fileName)

        stream.use { input ->
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun isValidLocale(code: String): Boolean {
        val locale: Locale = Locale.forLanguageTag(code)

        return Locale.getAvailableLocales().asList().contains(locale)
    }
}