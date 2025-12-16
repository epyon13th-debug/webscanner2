package com.example.webscannerapplication.css

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class CssScraper(private val client: OkHttpClient) {
    suspend fun getCssUrls(url: String): List<String>{
        return withContext(Dispatchers.IO){
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val html = response.body?.string()
                    if (html != null) {

                        val document = Jsoup.parse(html, url)

                        val linkElements = document.select("link[rel=stylesheet][href]")

                        return@withContext linkElements.map { it.attr("abs:href") }
                    }
                }
                return@withContext emptyList<String>()
            }catch (e: Exception) {
                e.printStackTrace()

                Log.e("CssScraper", "抓取失敗: ${e.message}")
                return@withContext emptyList<String>()
            }
        }
    }
}