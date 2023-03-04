package com.autio.android_app.ui.subscribe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.autio.android_app.R
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter

class SliderAdapter(
    textListTitle: ArrayList<String>,
    textList: ArrayList<String>,
) :
    SliderViewAdapter<SliderAdapter.SliderViewHolder>() {

    private var titleList: ArrayList<String> =
        textListTitle
    private var textSubList: ArrayList<String> =
        textList
    private val images =
        intArrayOf(
            R.drawable.photo_slider1,
            R.drawable.photo_slider2,
            R.drawable.photo_slider3,
            R.drawable.photo_slider4
        )

    override fun getCount() =
        titleList.size

    override fun onCreateViewHolder(
        parent: ViewGroup
    ): SliderViewHolder {
        val inflate: View =
            LayoutInflater.from(
                parent.context
            )
                .inflate(
                    R.layout.slider_item,
                    parent,
                    false
                )
        return SliderViewHolder(
            inflate
        )
    }

    override fun onBindViewHolder(
        viewHolder: SliderViewHolder,
        position: Int
    ) {
        viewHolder.textViewTitle.text =
            titleList[position]
        viewHolder.textViewSubText.text =
            textSubList[position]
        Glide.with(
            viewHolder.imageView
        )
            .load(
                images[position]
            )
            .fitCenter()
            .into(
                viewHolder.imageView
            )
    }

    class SliderViewHolder(
        itemView: View
    ) : ViewHolder(
        itemView
    ) {
        var imageView: ImageView =
            itemView.findViewById(
                R.id.myimage
            )
        var textViewTitle: TextView =
            itemView.findViewById(
                R.id.tvOptionTitle
            )
        var textViewSubText: TextView =
            itemView.findViewById(
                R.id.tvSubText
            )
    }
}