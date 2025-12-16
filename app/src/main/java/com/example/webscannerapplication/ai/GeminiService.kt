package com.example.webscannerapplication.ai

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class GeminiService(private val context: Context) {

    private val apiKey: String by lazy { loadApiKey() }

    private val model: GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey // 從安全儲存讀取的 Key
        )
    }
    private fun loadApiKey(): String {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            val prefs = EncryptedSharedPreferences.create(
                "secure_prefs", // 必須與 ApiKeySettingsActivity.PREFS_FILE_NAME 一致
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            return prefs.getString(ApiKeySettingsActivity.API_KEY_NAME, "") ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    suspend fun analyzeCode(code: String): String = withContext(Dispatchers.IO) {

        val prompt = """

請分析以下程式碼片段。請提供簡潔的總結，包含以下幾點：

1. 主要功能是什麼？

2. 是否涉及任何敏感操作 (例如：API keys, Cookie/localStorage 讀寫, 外部追蹤)？

3. 程式碼的複雜度 (低/中/高)。

---

[程式碼]

$code

""".trimIndent()

        try {
            val response = model.generateContent(prompt)
            return@withContext response.text ?: "Gemini 分析失敗或未提供內容。"
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "與 Gemini 服務連線失敗: ${e.message}"
        }
    }
}