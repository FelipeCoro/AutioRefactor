package com.autio.android_app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.databinding.StoryItemBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class StoryAdapter(
    private var stories: List<Story>
) :
    RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(
        val binding: StoryItemBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun render(
            model: Story
        ) {
            Glide.with(
                binding.root
            )
                .load(
                    model.imageUrl
                )
                .apply(
                    RequestOptions().placeholder(
                        R.drawable.maps_placeholder
                    ).error(
                        R.drawable.maps_placeholder
                    )
                )
                .into(
                    binding.storyImage
                )
            binding.storyTitle.text =
                model.title
            binding.storyAuthor.text =
                model.author
            binding.storyPin.setImageResource(
                R.drawable.ic_non_listened_pin
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoryViewHolder {
        return StoryViewHolder(
            StoryItemBinding
                .inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(
        holder: StoryViewHolder,
        position: Int
    ) {
        val story =
            stories[position]
        holder.render(
            story
        )
    }

    override fun getItemCount(): Int =
        stories.size

    fun getAllData(
        stories: List<Story>
    ) {
        this.stories =
            stories
    }
}