package com.ayudevices.neosynkparent.utils

import android.util.Log
import com.google.mlkit.nl.translate.*
import com.google.mlkit.common.model.DownloadConditions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.util.*

// Data class for languages
data class Language(
    val name: String,
    val code: String,
    val locale: Locale
)

// Translation Manager Class
class MLKitTranslationManager {
    private val translators = mutableMapOf<String, Translator>()

    suspend fun translateFromEnglish(text: String, toLanguage: Language): String {
        if (toLanguage.code == "en") return text

        return withContext(Dispatchers.IO) {
            try {
                val targetLanguageCode = mapLanguageCodeToMLKit(toLanguage.code)
                if (targetLanguageCode == null) {
                    Log.w("Translation", "Language ${toLanguage.code} not supported by ML Kit")
                    return@withContext text
                }

                val translator = getOrCreateTranslator("en", targetLanguageCode)

                // Ensure model is downloaded
                ensureModelDownloaded(translator)

                // Perform translation
                val result = translator.translate(text).await()
                Log.d("Translation", "Translated text: $result")
                result

            } catch (e: Exception) {
                Log.e("Translation", "Error translating text from English", e)
                text // Return original text on error
            }
        }
    }

    suspend fun translateToEnglish(text: String, fromLanguage: Language): String {
        if (fromLanguage.code == "en") return text

        return withContext(Dispatchers.IO) {
            try {
                val sourceLanguageCode = mapLanguageCodeToMLKit(fromLanguage.code)
                if (sourceLanguageCode == null) {
                    Log.w("Translation", "Language ${fromLanguage.code} not supported by ML Kit")
                    return@withContext text
                }

                val translator = getOrCreateTranslator(sourceLanguageCode, "en")

                // Ensure model is downloaded
                ensureModelDownloaded(translator)

                // Perform translation
                val result = translator.translate(text).await()
                Log.d("Translation", "Translated to English: $result")
                result

            } catch (e: Exception) {
                Log.e("Translation", "Error translating text to English", e)
                text // Return original text on error
            }
        }
    }

    private fun getOrCreateTranslator(sourceLanguage: String, targetLanguage: String): Translator {
        val key = "${sourceLanguage}_${targetLanguage}"

        return translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()
            Translation.getClient(options)
        }
    }

    private suspend fun ensureModelDownloaded(translator: Translator) {
        val conditions = DownloadConditions.Builder()
            .requireWifi() // Optional: only download on WiFi
            .build()

        translator.downloadModelIfNeeded(conditions).await()
    }

    private fun mapLanguageCodeToMLKit(code: String): String? {
        return when (code) {
            "kn" -> TranslateLanguage.KANNADA
            "hi" -> TranslateLanguage.HINDI
            "ta" -> TranslateLanguage.TAMIL
            "te" -> TranslateLanguage.TELUGU
            "ur" -> TranslateLanguage.URDU
            "en" -> TranslateLanguage.ENGLISH
            else -> null // Language not supported
        }
    }

    fun closeTranslators() {
        translators.values.forEach { it.close() }
        translators.clear()
    }

    suspend fun predownloadLanguageModel(languageCode: String) {
        try {
            val targetLanguage = mapLanguageCodeToMLKit(languageCode)
            if (targetLanguage != null) {
                val translator = getOrCreateTranslator("en", targetLanguage)

                val conditions = DownloadConditions.Builder()
                    .requireWifi()
                    .build()

                translator.downloadModelIfNeeded(conditions).await()
                Log.d("Translation", "Model downloaded for $languageCode")
            }
        } catch (e: Exception) {
            Log.e("Translation", "Failed to download model for $languageCode", e)
        }
    }

    fun isLanguageSupported(languageCode: String): Boolean {
        return mapLanguageCodeToMLKit(languageCode) != null
    }
}