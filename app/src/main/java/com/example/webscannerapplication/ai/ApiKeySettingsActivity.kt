package com.example.webscannerapplication.ai

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.webscannerapplication.R

class ApiKeySettingsActivity : AppCompatActivity() {
    private lateinit var apiKeyInput: EditText
    private lateinit var saveButton: Button

    companion object {
        private const val PREFS_FILE_NAME = "secure_prefs"
        const val API_KEY_NAME = "gemini_api_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.api_key_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        apiKeyInput = findViewById(R.id.apiKeyInput)
        saveButton = findViewById(R.id.saveApiKeyButton)

        loadApiKey()
        saveButton.setOnClickListener {
            saveApiKey(apiKeyInput.text.toString().trim())
        }
    }

    private fun getEncryptedSharedPreferences(context: Context): EncryptedSharedPreferences {
              val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            PREFS_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    private fun loadApiKey() {
        val prefs = getEncryptedSharedPreferences(this)

        val currentKey = prefs.getString(API_KEY_NAME, "")
        apiKeyInput.setText(currentKey)
    }

    private fun saveApiKey(key: String) {
        if (key.isEmpty()) {
            Toast.makeText(this, "API 金鑰不能為空", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getEncryptedSharedPreferences(this)
        prefs.edit()
            .putString(API_KEY_NAME, key)
            .apply()

        Toast.makeText(this, "API 金鑰已加密儲存！", Toast.LENGTH_SHORT).show()

        finish()
    }
}