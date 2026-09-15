package com.dunamis.world.lspalm.server

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dunamis.world.lspalm.databinding.SchoolListDialogNameBinding
import com.dunamis.world.lspalm.statics.URLS

class ServerDisplayAdapter(
    c: Context,
    alKeysList: ArrayList<String>,
    alValueList: ArrayList<String>,
    alImageList: ArrayList<String>
) :
    RecyclerView.Adapter<ServerDisplayAdapter.ViewHolder>() {
    private val alKeysList: ArrayList<String>
    private val alValueList: ArrayList<String>
    private val alImageList: ArrayList<String>
    private val serverTap: ServerTap

    init {
        serverTap = c as ServerTap
        this.alKeysList = alKeysList
        this.alValueList = alValueList
        this.alImageList = alImageList
    }

    internal interface ServerTap {
        fun serverTapped(baseUrlRaw: String?, baseUrl: String?, imgUrl: String?, logoImgUrl: String?, imgUrlFace: String?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            SchoolListDialogNameBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == alKeysList.size - 1) {
            holder.b.view.visibility = View.GONE
        }
        holder.b.tvServerNames.text = alKeysList[position]
        holder.b.tvServerNames.setOnClickListener {
            val imageUrl: String
            val logoImageUrl: String
            val imgUrlFace: String
            val baseUrlRaw = alValueList[position]
            val baseUrl = alValueList[position] + URLS().BASE_URL_APPEND
            if (alImageList.size == 0) {

                imageUrl = alValueList[position] + "uploads/std/"
                logoImageUrl = alValueList[position] + "uploads/logo_image/"
                imgUrlFace = alImageList[position] + "uploads/faceimages/"
            } else {

                imageUrl = alImageList[position] + "uploads/std/"
                logoImageUrl = alImageList[position] + "uploads/logo_image/"
                imgUrlFace = alImageList[position] + "uploads/faceimages/"
            }
            serverTap.serverTapped(baseUrlRaw, baseUrl, imageUrl, logoImageUrl, imgUrlFace)
        }
    }

    override fun getItemCount(): Int {
        return alKeysList.size
    }

    class ViewHolder(b: SchoolListDialogNameBinding) : RecyclerView.ViewHolder(b.root) {
        val b: SchoolListDialogNameBinding

        init {
            this.b = b
        }
    }
}