package com.autio.android_app.ui.stories.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.databinding.ItemListPopupWindowBinding

class StoryOptionsAdapter<T>(
    private val story: T,
    private var options: List<com.autio.android_app.data.api.model.StoryOption>,
    private val onStoryOptionClickListener: com.autio.android_app.data.api.model.StoryOptionClickListener<T>? = null
) :
    RecyclerView.Adapter<StoryOptionsAdapter<T>.OptionViewHolder>() {

    inner class OptionViewHolder(
        val binding: ItemListPopupWindowBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun render(
            model: com.autio.android_app.data.api.model.StoryOption
        ) {
            binding.root.setOnClickListener {
                onStoryOptionClickListener?.onItemClick(
                    model,
                    story
                )
            }
            binding.ivOptionIcon.setImageResource(
                model.option.resourceId
            )
            binding.tvOptionTitle.text =
                model.option.title
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) =
        OptionViewHolder(
            ItemListPopupWindowBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: OptionViewHolder,
        position: Int
    ) {
        val option =
            options[position]
        holder.render(
            option
        )
    }

    override fun getItemCount() =
        options.size
}
