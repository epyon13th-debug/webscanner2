package com.example.webscannerapplication.pic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.webscannerapplication.R

interface ImgItemClickListener {
    fun onViewClicked(url: String)
    fun onDownloadClicked(url: String)
}
class ImgListAdapter(
    private val context: Context,
    private val imgList: List<String>,
    private val listener: ImgItemClickListener
) : RecyclerView.Adapter<ImgListAdapter.ImgViewHolder>() {

    class ImgViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUrl: TextView = view.findViewById(R.id.img_item)
        val btnView: Button = view.findViewById(R.id.btn_view_img)
        val btnDownload: Button = view.findViewById(R.id.btn_download_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImgViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.img_item_view, parent, false)
        return ImgViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImgViewHolder, position: Int) {
        val url = imgList[position]
        holder.tvUrl.text = url
        holder.btnView.setOnClickListener {
            listener.onViewClicked(url)
        }
        holder.btnDownload.setOnClickListener {
            listener.onDownloadClicked(url)
        }
    }

    override fun getItemCount() = imgList.size
}