package com.example.webscannerapplication

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.webscannerapplication.ai.ApiKeySettingsActivity
import okhttp3.OkHttpClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.webscannerapplication.css.CssListActivity
import com.example.webscannerapplication.css.CssScraper
import com.example.webscannerapplication.js.JsListActivity
import com.example.webscannerapplication.js.JsScraper
import com.example.webscannerapplication.pic.ImgListActivity
import com.example.webscannerapplication.pic.ImgScraper

class MainActivity : AppCompatActivity() {
    lateinit var url_addr: EditText
    lateinit var url_past: Button
    lateinit var ez_chose: RadioGroup
    lateinit var webSite: WebView
    lateinit var scanJs: FloatingActionButton
    lateinit var scanCss: FloatingActionButton
    lateinit var scanImg: FloatingActionButton
    lateinit var apiSet: FloatingActionButton
    private val client = OkHttpClient()
    private lateinit var jsScraper: JsScraper
    private lateinit var cssScraper: CssScraper
    private lateinit var imgScraper: ImgScraper

    companion object {
        const val EXTRA_CLEANUP_FLAG = "extra_cleanup_flag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViews()
        setupWebView()
        jsScraper = JsScraper(client)
        cssScraper = CssScraper(client)
        imgScraper = ImgScraper(client)
        setupListeners()
    }

    fun findViews() {
        url_addr = findViewById(R.id.url_In)
        url_past = findViewById(R.id.url_Past)
        ez_chose = findViewById(R.id.ez_chose)
        webSite = findViewById(R.id.web_View)
        scanJs = findViewById(R.id.js_Scanner)
        scanCss = findViewById(R.id.css_Scanner)
        scanImg = findViewById(R.id.img_Scanner)
        apiSet = findViewById(R.id.api_Keyset)
    }

    private fun setupWebView() {
        webSite.webViewClient = WebViewClient()
        webSite.settings.javaScriptEnabled = true
        webSite.settings.domStorageEnabled = true
    }

    private fun getTargetUrl(): String {
        val currentWebUrl = webSite.url
        val inputUrl = url_addr.text.toString().trim()

        return if (!currentWebUrl.isNullOrEmpty() && (currentWebUrl.startsWith("http") || currentWebUrl.startsWith(
                "https"
            ))
        ) {
            currentWebUrl
        } else {
            inputUrl
        }
    }

    private fun setupListeners() {

        url_past.setOnClickListener {
            var url = url_addr.text.toString().trim()

            if (url.isNotEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                webSite.loadUrl(url)

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }
        url_past.setOnLongClickListener {
            Toast.makeText(this, "關閉應用程式", Toast.LENGTH_SHORT).show()
            finishAffinity()
            return@setOnLongClickListener true
        }

        ez_chose.setOnCheckedChangeListener { group, checkedId ->
            var selectedUrl = ""
            when (checkedId) {
                R.id.ez_Url1 -> selectedUrl = "https://www.must.edu.tw/"
                R.id.ez_Url2 -> selectedUrl = "https://www.gamer.com.tw/"
                R.id.ez_Url3 -> selectedUrl = "https://udn.com/news/index/"
            }
            if (selectedUrl.isNotEmpty()) {
                webSite.loadUrl(selectedUrl)
                url_addr.setText(selectedUrl)
            }
        }
        scanJs.setOnClickListener {
            val targetUrl = getTargetUrl()

            if (targetUrl.startsWith("http") || targetUrl.startsWith("https")) {
                performJsScan(targetUrl)
            } else {
                Toast.makeText(this, "請輸入或載入有效網址", Toast.LENGTH_SHORT).show()
            }
        }
        scanCss.setOnClickListener {
            val targetUrl = getTargetUrl()

            if (targetUrl.startsWith("http") || targetUrl.startsWith("https")) {
                performCssScan(targetUrl)
            } else {
                Toast.makeText(this, "請輸入或載入有效網址", Toast.LENGTH_SHORT).show()
            }
        }
        scanImg.setOnClickListener {
            val targetUrl = getTargetUrl()

            if (targetUrl.startsWith("http") || targetUrl.startsWith("https")) {
                performImgScan(targetUrl)
            } else {
                Toast.makeText(this, "請輸入或載入有效網址", Toast.LENGTH_SHORT).show()
            }
        }
        apiSet.setOnClickListener {
            val intent = Intent(this@MainActivity, ApiKeySettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performJsScan(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val jsList = jsScraper.getJsUrls(url)

            if (jsList.isNotEmpty()) {
                val intent = Intent(this@MainActivity, JsListActivity::class.java).apply {
                    putStringArrayListExtra(JsListActivity.EXTRA_JS_LIST, ArrayList(jsList))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this@MainActivity, "掃描完成，未發現外部 JS 檔案", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun performCssScan(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val cssList = cssScraper.getCssUrls(url)

            if (cssList.isNotEmpty()) {
                val intent = Intent(this@MainActivity, CssListActivity::class.java).apply {
                    putStringArrayListExtra(CssListActivity.EXTRA_CSS_LIST, ArrayList(cssList))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this@MainActivity, "掃描完成，未發現外部 CSS 檔案", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun performImgScan(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@MainActivity, "正在掃描圖片連結...", Toast.LENGTH_SHORT).show()

            val imgList = imgScraper.getImgUrls(url)

            if (imgList.isNotEmpty()) {
                val intent = Intent(this@MainActivity, ImgListActivity::class.java).apply {
                    putStringArrayListExtra(ImgListActivity.EXTRA_IMG_LIST, ArrayList(imgList))
                }
                startActivity(intent)
            } else {
                Toast.makeText(this@MainActivity, "掃描完成，未發現圖片連結", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val cleanupFlag = intent.getBooleanExtra(EXTRA_CLEANUP_FLAG, false)
        if (cleanupFlag) {
            clearData()
        }
    }

    private fun clearData() {
        url_addr.setText("")
        webSite.loadUrl("about:blank")
        Toast.makeText(this, "已返回主頁並清除資料", Toast.LENGTH_SHORT).show()
    }
}