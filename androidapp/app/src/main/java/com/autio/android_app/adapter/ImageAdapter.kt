package com.autio.android_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.animateFlip
import com.autio.android_app.data.model.design.LocationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ImageAdapter(
    private val dataset: List<LocationView>
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    class ImageViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(
        view
    ) {
        val imageView: ImageView =
            view.findViewById(
                R.id.image_name
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageViewHolder {
        val adapterLayout =
            LayoutInflater.from(
                parent.context
            )
                .inflate(
                    R.layout.image_item,
                    parent,
                    false
                )

        return ImageViewHolder(
            adapterLayout
        )
    }

    override fun onBindViewHolder(
        holder: ImageViewHolder,
        position: Int
    ) {
        val fakePosition = position % dataset.size
        val item =
            dataset[fakePosition]
        holder.imageView.setImageResource(item.resourceId)

        holder.imageView.animateFlip()
    }

    override fun getItemCount(): Int = Integer.MAX_VALUE
}