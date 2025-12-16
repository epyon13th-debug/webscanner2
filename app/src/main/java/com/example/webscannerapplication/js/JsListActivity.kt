package com.example.webscannerapplication.js

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.webscannerapplication.ai.GeminiService
import com.example.webscannerapplication.MainActivity
import com.example.webscannerapplication.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class JsListActivity : AppCompatActivity(), JsItemClickListener {

    lateinit var backFrompage: FloatingActionButton
    lateinit var recyclerView: RecyclerView
    lateinit var jsView: WebView
    private val client = OkHttpClient()
    private val geminiService = GeminiService(this)

    companion object {
        const val EXTRA_JS_LIST = "extra_js_list"

    }

    private val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.show_js_list)

        val jsList = intent.getStringArrayListExtra(EXTRA_JS_LIST)
        if (jsList.isNullOrEmpty()) {
            Toast.makeText(this, "未找到任何 JS 檔案", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        recyclerView = findViewById(R.id.jsRecy)
        jsView = findViewById(R.id.jsView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        setupWebView()

        val adapter = JsListAdapter(this, jsList, this)
        recyclerView.adapter = adapter

        backFrompage = findViewById(R.id.jsBack)
        backFrompage.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java).apply {
                 flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(MainActivity.Companion.EXTRA_CLEANUP_FLAG, true)
            }
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.jsRecy)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun setupWebView() {
        jsView.webViewClient = WebViewClient()
        jsView.settings.javaScriptEnabled = true
    }

    override fun onViewClicked(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@JsListActivity, "正在載入 $url...", Toast.LENGTH_SHORT).show()
            val jsContent = downloadJsContent(url)
            if (jsContent != null) {
                displayCodeInWebView(jsContent)
            } else {
                Toast.makeText(this@JsListActivity, "無法下載或解析 JS 內容", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private suspend fun downloadJsContent(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        return@withContext response.body?.string()
                    }
                }
            } catch (e: Exception) {
                Log.e("JsListActivity", "下載 JS 內容失敗: ${e.message}")
            }
            return@withContext null
        }
    }
    private fun displayCodeInWebView(code: String) {
        val htmlContent = """
            <html>
            <head>
                <style>
                    body { font-family: monospace; padding: 10px; white-space: pre-wrap; background-color: #282c34; color: #abb2bf; }
                    pre { margin: 0; }
                </style>
            </head>
            <body>
                <pre>${code.replace("<", "&lt;").replace(">", "&gt;")}</pre>
            </body>
            </html>
        """.trimIndent()

        jsView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }


    override fun onDownloadClicked(url: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            checkStoragePermission()
            Toast.makeText(this, "請先授予儲存權限。", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(url)).apply {
            }
            downloadManager.enqueue(request)
            Toast.makeText(this, "開始下載 ...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("Download", "下載失敗: ${e.message}")
            Toast.makeText(this, "下載失敗，請檢查網址或權限", Toast.LENGTH_LONG).show()
        }
    }

    override fun onAnalyzeClicked(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@JsListActivity, "正在下載並傳送內容給 Gemini 進行分析...", Toast.LENGTH_LONG).show()

            val jsContent = downloadJsContent(url)

            if (jsContent != null) {
                val analysisResult = geminiService.analyzeCode(jsContent)
                displayCodeInWebView("--- Gemini 分析報告 --- \n\n$analysisResult")
            } else {
                Toast.makeText(this@JsListActivity, "無法下載內容進行分析", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "儲存權限已授予，請再次嘗試下載。", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "下載需要儲存權限，功能已禁用。", Toast.LENGTH_LONG).show()
            }
        }
    }
}