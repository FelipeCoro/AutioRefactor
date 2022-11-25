package com.autio.android_app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.model.story.StoryResponse
import com.autio.android_app.databinding.StoryItemBinding

class StoryAdapter(
    private var stories: List<StoryResponse>
) :
    RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    class StoryViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(
        itemView
    ) {
        val binding =
            StoryItemBinding.bind(
                itemView
            )

        fun render(
            model: StoryResponse
        ) {
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
            LayoutInflater.from(
                parent.context
            )
                .inflate(
                    R.layout.fragment_map_playlist,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(
        holder: StoryViewHolder,
        position: Int
    ) {
        Log.d(
            "STORIES",
            "$stories"
        )
        val story =
            stories[position]
        holder.render(
            story
        )
    }

    override fun getItemCount(): Int =
        stories.size

    fun getAllData(
        stories: List<StoryResponse>
    ) {
        this.stories =
            stories
    }
}