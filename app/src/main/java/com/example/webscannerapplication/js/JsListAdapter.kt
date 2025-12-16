package com.example.webscannerapplication.js

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.webscannerapplication.R

interface JsItemClickListener {
    fun onViewClicked(url: String)
    fun onDownloadClicked(url: String)
    fun onAnalyzeClicked(url: String)
}

class JsListAdapter(
    private val context: Context,
    private val jsList: List<String>,
    private val listener: JsItemClickListener
) : RecyclerView.Adapter<JsListAdapter.JsViewHolder>() {

    class JsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUrl: TextView = view.findViewById(R.id.Js_item)
        val btnView: Button = view.findViewById(R.id.btn_view_js)
        val btnDownload: Button = view.findViewById(R.id.btn_download_js)
        val btnAnalyze: Button = view.findViewById(R.id.Js_ai)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.js_item_view, parent, false)
        return JsViewHolder(view)
    }

    override fun onBindViewHolder(holder: JsViewHolder, position: Int) {
        val url = jsList[position]
        holder.tvUrl.text = url

        holder.btnView.setOnClickListener {
            listener.onViewClicked(url)
        }

        holder.btnDownload.setOnClickListener {
            listener.onDownloadClicked(url)
        }

        holder.btnAnalyze.setOnClickListener {
            listener.onAnalyzeClicked(url)
        }
    }

    override fun getItemCount() = jsList.size
}