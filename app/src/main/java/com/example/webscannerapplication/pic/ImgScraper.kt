package com.example.webscannerapplication.pic

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

class ImgScraper(private val client: OkHttpClient) {
    suspend fun getImgUrls(url: String): List<String>{
        return withContext(Dispatchers.IO){
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val html = response.body?.string()
                    if (html != null) {
                        val document = Jsoup.parse(html, url)
                        val elements = document.select("a[href], img[src]")

                        return@withContext elements.mapNotNull{
                            val src = it.attr("abs:src").ifEmpty { it.attr("abs:href") }
                            // 檢查是否包含特定的副檔名
                            if (src.endsWith(".jpg", ignoreCase = true) ||
                                src.endsWith(".png", ignoreCase = true) ||
                                src.endsWith(".svg", ignoreCase = true)) {
                                src
                            } else {
                                null
                            }

                        }
                    }
                }
                return@withContext emptyList<String>()
            }catch (e: Exception) {
                e.printStackTrace()
                Log.e("ImgScraper", "抓取圖片連結失敗: ${e.message}")
                return@withContext emptyList<String>()
            }
        }
    }
}