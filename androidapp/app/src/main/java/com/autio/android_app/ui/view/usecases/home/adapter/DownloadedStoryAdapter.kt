package com.autio.android_app.ui.view.usecases.home.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.model.StoryOption
import com.autio.android_app.data.model.story.DownloadedStory
import com.autio.android_app.util.showStoryOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File

class DownloadedStoryAdapter(
    private var onStoryPlay: ((String) -> Unit)?,
    private var onOptionClick: ((StoryOption, DownloadedStory) -> Unit)?
) : ListAdapter<DownloadedStory, DownloadedStoryAdapter.DownloadedStoryViewHolder>(
    DownloadedStoryComparator()
) {

    class DownloadedStoryViewHolder(
        itemView: View,
        private var onStoryPlay: ((String) -> Unit)?,
        private var onOptionClick: ((StoryOption, DownloadedStory) -> Unit)?
    ) : RecyclerView.ViewHolder(
        itemView
    ) {
        private val ivStoryImage =
            itemView.findViewById<ImageView>(
                R.id.story_image
            )
        private val ivStoryCard =
            itemView.findViewById<CardView>(
                R.id.ivStoryCard
            )
        private val storyTitle =
            itemView.findViewById<TextView>(
                R.id.story_title
            )
        private val storyAuthor =
            itemView.findViewById<TextView>(
                R.id.story_author
            )
        private val storyPin =
            itemView.findViewById<ImageView>(
                R.id.story_pin
            )
        private val ivStoryItemOptions =
            itemView.findViewById<ImageView>(
                R.id.ivStoryItemOptions
            )

        fun bind(
            model: DownloadedStory
        ) {
            Glide.with(
                itemView
            )
                .load(
                    model.image?.let { path ->
                        Uri.parse(
                            path
                        ).path?.let {
                            File(
                                it
                            )
                        }
                    })
                .apply(
                    RequestOptions().placeholder(
                        R.drawable.maps_placeholder
                    )
                        .error(
                            R.drawable.maps_placeholder
                        )
                )
                .into(
                    ivStoryImage
                )
            ivStoryCard.setOnClickListener {
                onStoryPlay?.invoke(
                    model.id
                )
            }
            storyTitle.text =
                model.title
            storyAuthor.text =
                model.author
            storyPin.setImageResource(
                R.drawable.ic_non_listened_pin
            )
            ivStoryItemOptions.setOnClickListener {
                showStoryOptions(
                    itemView.context,
                    itemView.parent as ViewGroup,
                    it,
                    model,
                    arrayListOf(
                        StoryOption.DELETE,
                        if (model.isBookmarked == true) StoryOption.REMOVE_BOOKMARK else StoryOption.BOOKMARK,
                        if (model.isLiked == true) StoryOption.REMOVE_LIKE else StoryOption.LIKE,
                        StoryOption.REMOVE_DOWNLOAD,
                        StoryOption.DIRECTIONS,
                        StoryOption.SHARE
                    ),
                    onOptionClick = onOptionClick
                )
            }
        }

        companion object {
            fun create(
                parent: ViewGroup,
                onStoryPlay: ((String) -> Unit)?,
                onOptionClick: ((StoryOption, DownloadedStory) -> Unit)?
            ): DownloadedStoryViewHolder {
                val view =
                    LayoutInflater.from(
                        parent.context
                    )
                        .inflate(
                            R.layout.story_item,
                            parent,
                            false
                        )
                return DownloadedStoryViewHolder(
                    view,
                    onStoryPlay,
                    onOptionClick
                )
            }
        }
    }

    class DownloadedStoryComparator :
        DiffUtil.ItemCallback<DownloadedStory>() {
        override fun areItemsTheSame(
            oldItem: DownloadedStory,
            newItem: DownloadedStory
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: DownloadedStory,
            newItem: DownloadedStory
        ): Boolean {
            return oldItem.recordPath == newItem.recordPath
                    || oldItem.isLiked == newItem.isLiked
                    || oldItem.isBookmarked == newItem.isBookmarked
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DownloadedStoryViewHolder {
        return DownloadedStoryViewHolder.create(
            parent,
            onStoryPlay,
            onOptionClick
        )
    }

    override fun onBindViewHolder(
        holder: DownloadedStoryViewHolder,
        position: Int
    ) {
        val story =
            getItem(
                position
            )
        holder.bind(
            story
        )
    }
}