package com.example.webscannerapplication.pic

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
import android.os.Environment
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.webscannerapplication.MainActivity
import com.example.webscannerapplication.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.OkHttpClient

class ImgListActivity : AppCompatActivity(), ImgItemClickListener {
    lateinit var backFrompage: FloatingActionButton
    lateinit var recyclerView: RecyclerView
    lateinit var imgView: WebView
    private val client = OkHttpClient()

    companion object {
        const val EXTRA_IMG_LIST = "extra_img_list"
    }

    private val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.show_img_list)
        val imgList = intent.getStringArrayListExtra(EXTRA_IMG_LIST)
        if (imgList.isNullOrEmpty()) {
            Toast.makeText(this, "未找到任何圖片檔案", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.imgRecy)
        imgView = findViewById(R.id.imgView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        setupWebView()

        val adapter = ImgListAdapter(this, imgList, this)
        recyclerView.adapter = adapter

        backFrompage = findViewById(R.id.imgBack)
        backFrompage.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(MainActivity.Companion.EXTRA_CLEANUP_FLAG, true)
            }
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.imgRecy)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun setupWebView() {
        imgView.webViewClient = WebViewClient()
        imgView.settings.javaScriptEnabled = true
        imgView.settings.builtInZoomControls = true
        imgView.settings.displayZoomControls = false
    }
    override fun onViewClicked(url: String) {
        displayImageInWebView(url)
    }
    private fun displayImageInWebView(imageUrl: String) {
        val htmlContent = """
            <html>
            <head>
                <style>
                    body { margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; background-color: #f0f0f0; height: 100vh; }
                    img { max-width: 100%; max-height: 100%; object-fit: contain; }
                </style>
            </head>
            <body>
                <img src="$imageUrl" alt="無法載入圖片" onerror="this.src='https://placehold.co/400x200/FF0000/FFFFFF?text=Image+Load+Error'">
            </body>
            </html>
        """.trimIndent()

        imgView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        Toast.makeText(this, "正在載入圖片 $imageUrl", Toast.LENGTH_SHORT).show()
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
                val fileName = url.substringAfterLast('/').substringBefore('?').ifEmpty { "downloaded_image_${System.currentTimeMillis()}.png" }

                setTitle(fileName)
                setDescription("正在下載圖片檔案: $fileName")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "IMAGES_Files/$fileName")
            }
            downloadManager.enqueue(request)
            Toast.makeText(this, "開始下載圖片檔案...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("ImgListActivity", "下載失敗: ${e.message}")
            Toast.makeText(this, "下載失敗，請檢查網址或權限", Toast.LENGTH_LONG).show()
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