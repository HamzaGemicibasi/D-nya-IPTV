package com.example.network

import com.example.model.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object M3uParser {
    private val client = OkHttpClient()

    suspend fun parseFromUrl(url: String): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val inputStream = response.body?.byteStream() ?: return@withContext emptyList()
                val reader = BufferedReader(InputStreamReader(inputStream))
                
                var name = ""
                var category = "Genel"
                var logoUrl = ""
                
                val logoRegex = """tvg-logo="([^"]+)"""".toRegex()
                val groupRegex = """group-title="([^"]+)"""".toRegex()
                
                reader.useLines { lines ->
                    lines.forEach { line ->
                        val trimmedLine = line.trim()
                        if (trimmedLine.startsWith("#EXTINF")) {
                            // Extract name (it's after the last comma)
                            val commaIndex = trimmedLine.lastIndexOf(',')
                            name = if (commaIndex != -1) {
                                trimmedLine.substring(commaIndex + 1).trim()
                            } else {
                                "Bilinmeyen Kanal"
                            }
                            
                            // Extract logo
                            val logoMatch = logoRegex.find(trimmedLine)
                            logoUrl = logoMatch?.groupValues?.get(1) ?: ""
                            
                            // Extract group (category)
                            val groupMatch = groupRegex.find(trimmedLine)
                            category = groupMatch?.groupValues?.get(1) ?: "Genel"
                        } else if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                            // This is the stream URL
                            if (name.isNotEmpty()) {
                                channels.add(
                                    Channel(
                                        name = name,
                                        category = category,
                                        url = trimmedLine,
                                        logoUrl = logoUrl
                                    )
                                )
                                // Reset variables for next channel
                                name = ""
                                category = "Genel"
                                logoUrl = ""
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        channels
    }
}
