package com.autio.android_app.ui.stories.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.api.model.modelLegacy.LocationView
import com.autio.android_app.extensions.animateFlip

class ImageAdapter(
    private val dataset: List<LocationView>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false)

        return ImageViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val fakePosition = position % dataset.size
        val item = dataset[fakePosition]
        holder.imageView.setImageResource(item.resourceId)

        holder.imageView.animateFlip()
    }

    override fun getItemCount(): Int =
        Integer.MAX_VALUE
}
