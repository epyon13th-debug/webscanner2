package com.example.webscannerapplication.js

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class JsScraper (private val client: OkHttpClient){
    suspend fun getJsUrls(url: String): List<String>{
        return withContext(Dispatchers.IO){
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val html = response.body?.string()
                    if (html != null) {
                        // Jsoup 解析
                        val document = Jsoup.parse(html)
                        val scriptElements = document.select("script[src]")

                        return@withContext scriptElements.map { it.attr("src") }
                    }
                }
                return@withContext emptyList<String>()
            }catch (e: Exception) {
                e.printStackTrace()
                Log.e("JsScraper", "抓取失敗: ${e.message}")
                return@withContext emptyList<String>()
            }
        }
    }

}