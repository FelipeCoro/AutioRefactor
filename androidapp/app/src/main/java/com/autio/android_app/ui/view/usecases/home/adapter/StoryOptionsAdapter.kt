package com.autio.android_app.ui.view.usecases.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.data.model.StoryOption
import com.autio.android_app.data.model.StoryOptionClickListener
import com.autio.android_app.databinding.ItemListPopupWindowBinding

class StoryOptionsAdapter<T>(
    private val story: T,
    private var options: List<StoryOption>,
    private val onStoryOptionClickListener: StoryOptionClickListener<T>? = null
) :
    RecyclerView.Adapter<StoryOptionsAdapter<T>.OptionViewHolder>() {

    inner class OptionViewHolder(
        val binding: ItemListPopupWindowBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun render(
            model: StoryOption
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