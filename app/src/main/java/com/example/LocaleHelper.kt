package com.example

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "agripro_locale"
    private const val LANGUAGE_KEY = "app_language"

    fun wrap(context: Context): Context {
        val language = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, "ar") ?: "ar"
        if (language == "system") return context
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    fun setLocale(context: Context, language: String) {
        require(language == "system" || language == "ar" || language == "fr" || language == "en")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(LANGUAGE_KEY, language).apply()
    }

    fun getLanguage(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, "system") ?: "system"
}
