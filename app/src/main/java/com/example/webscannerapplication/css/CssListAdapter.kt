package com.example.webscannerapplication.css

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.webscannerapplication.R

interface CssItemClickListener {
    fun onViewClicked(url: String)
    fun onDownloadClicked(url: String)
    fun onAnalyzeClicked(url: String)
}
class CssListAdapter(
    private val context: Context,
    private val cssList: List<String>,
    private val listener: CssItemClickListener
) : RecyclerView.Adapter<CssListAdapter.CssViewHolder>() {
    class CssViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUrl: TextView = view.findViewById(R.id.Css_item)
        val btnView: Button = view.findViewById(R.id.btn_view_css)
        val btnDownload: Button = view.findViewById(R.id.btn_download_css)
        val btnAnalyze: Button = view.findViewById(R.id.Css_ai)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CssViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.css_item_view, parent, false)
        return CssViewHolder(view)
    }

    override fun onBindViewHolder(holder: CssViewHolder, position: Int) {
        val url = cssList[position]
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

    override fun getItemCount() = cssList.size
}