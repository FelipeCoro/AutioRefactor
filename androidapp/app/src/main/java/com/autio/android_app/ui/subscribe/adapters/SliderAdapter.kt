package com.autio.android_app.ui.subscribe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.autio.android_app.R
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter

data class SliderAdapter(
    val titles: List<String>, val texts: List<String>, @DrawableRes val images: List<Int>
) : SliderViewAdapter<SliderAdapter.SliderViewHolder>() {
    override fun getCount() = titles.size

    override fun onCreateViewHolder(parent: ViewGroup): SliderViewHolder {
        val inflate: View =
            LayoutInflater.from(parent.context).inflate(R.layout.slider_item, parent, false)
        return SliderViewHolder(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderViewHolder, position: Int) {
        viewHolder.textViewTitle.text = titles[position]
        viewHolder.textViewSubText.text = texts[position]
        Glide.with(viewHolder.imageView).load(images[position]).fitCenter()
            .into(viewHolder.imageView)
    }

    class SliderViewHolder(itemView: View) : ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.myimage)
        var textViewTitle: TextView = itemView.findViewById(R.id.tvOptionTitle)
        var textViewSubText: TextView = itemView.findViewById(R.id.tvSubText)
    }
}