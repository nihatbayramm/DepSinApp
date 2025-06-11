package com.example.depsinn

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class ContentFilter private constructor() {
    private val yasakliKelimeler = mutableSetOf<String>()

    init {
        try {
            val inputStream = javaClass.classLoader?.getResourceAsStream("raw/karaliste.txt")
            if (inputStream != null) {
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let { yasakliKelimeler.add(it.trim()) }
                }
                reader.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Yasaklı kelimeler yüklenirken hata: ${e.message}")
        }
    }

    fun mesajiKontrolEt(mesaj: String, context: Context): Boolean {
        val kelimeler = mesaj.lowercase().split(" ")
        for (kelime in kelimeler) {
            if (yasakliKelimeler.contains(kelime)) {
                return false
            }
        }
        return true
    }

    fun mesajiFiltrele(mesaj: String): String {
        var filtrelenmisMesaj = mesaj
        val kelimeler = mesaj.lowercase().split(" ")
        
        for (kelime in kelimeler) {
            if (yasakliKelimeler.contains(kelime)) {
                val yildizlar = "*".repeat(kelime.length)
                filtrelenmisMesaj = filtrelenmisMesaj.replace(kelime, yildizlar, ignoreCase = true)
            }
        }
        
        return filtrelenmisMesaj
    }

    companion object {
        private const val TAG = "ContentFilter"
        private var instance: ContentFilter? = null

        fun getInstance(): ContentFilter {
            if (instance == null) {
                instance = ContentFilter()
            }
            return instance!!
        }
    }
} 