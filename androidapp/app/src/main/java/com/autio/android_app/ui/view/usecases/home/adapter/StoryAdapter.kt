package com.autio.android_app.ui.view.usecases.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.entities.story.Story
import com.autio.android_app.util.showStoryOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class StoryAdapter(
    private var playingStory: MutableLiveData<Story?>,
    private var onStoryPlay: ((String) -> Unit)?,
    private var onOptionClick: ((com.autio.android_app.data.api.model.StoryOption, Story) -> Unit)?,
    private var shouldPinLocationBeShown: Boolean = false
) :
    ListAdapter<Story, StoryAdapter.StoryViewHolder>(
        StoryComparator()
    ) {

    class StoryViewHolder(
        itemView: View,
        private val playingStory: MutableLiveData<Story?>,
        private val onStoryPlay: ((String) -> Unit)?,
        private val onOptionClick: ((com.autio.android_app.data.api.model.StoryOption, Story) -> Unit)?,
        private val shouldPinLocationBeShown: Boolean
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
        private val ivPlayIcon =
            itemView.findViewById<ImageView>(
                R.id.ivPlayIcon
            )

        fun bind(
            model: Story
        ) {
            val wasStoryListenedAtLeast30Seconds =
                model.listenedAtLeast30Secs == true
            Glide.with(
                itemView
            )
                .load(
                    model.imageUrl
                )
                .apply(
                    RequestOptions().placeholder(
                        R.color.autio_blue_20
                    )
                        .error(
                            R.color.autio_blue_20
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
            storyTitle.apply {
                text =
                    model.title
                if (wasStoryListenedAtLeast30Seconds) {
                    setTextColor(
                        resources.getColor(
                            R.color.autio_blue_60,
                            null
                        )
                    )
                }
            }
            storyAuthor.text =
                model.author
            if (shouldPinLocationBeShown) {
                storyPin.visibility =
                    View.VISIBLE
            }
            ivStoryItemOptions.setOnClickListener {
                showStoryOptions(
                    itemView.context,
                    itemView.parent as ViewGroup,
                    it,
                    model,
                    arrayListOf(
                        if (model.isBookmarked == true) com.autio.android_app.data.api.model.StoryOption.REMOVE_BOOKMARK else com.autio.android_app.data.api.model.StoryOption.BOOKMARK,
                        if (model.isLiked == true) com.autio.android_app.data.api.model.StoryOption.REMOVE_LIKE else com.autio.android_app.data.api.model.StoryOption.LIKE,
                        com.autio.android_app.data.api.model.StoryOption.DOWNLOAD,
                        com.autio.android_app.data.api.model.StoryOption.DIRECTIONS,
                        com.autio.android_app.data.api.model.StoryOption.SHARE
                    ),
                    onOptionClick = onOptionClick
                )
            }
            playingStory.observe(
                itemView.context as LifecycleOwner
            ) {
                if (it?.id == model.id) {
                    storyPin.setImageResource(
                        R.drawable.ic_now_listening_pin
                    )
                    ivPlayIcon.setImageResource(
                        R.drawable.ic_pause_mini
                    )
                } else {
                    storyPin.setImageResource(
                        if (wasStoryListenedAtLeast30Seconds) R.drawable.ic_listened_pin
                        else R.drawable.ic_non_listened_pin
                    )
                    ivPlayIcon.setImageResource(
                        R.drawable.ic_play_mini
                    )
                }
            }
        }

        companion object {
            fun create(
                parent: ViewGroup,
                playingStory: MutableLiveData<Story?>,
                onStoryPlay: ((String) -> Unit)?,
                onOptionClick: ((com.autio.android_app.data.api.model.StoryOption, Story) -> Unit)?,
                shouldPinLocationBeShown: Boolean
            ): StoryViewHolder {
                val view =
                    LayoutInflater.from(
                        parent.context
                    )
                        .inflate(
                            R.layout.story_item,
                            parent,
                            false
                        )
                return StoryViewHolder(
                    view,
                    playingStory,
                    onStoryPlay,
                    onOptionClick,
                    shouldPinLocationBeShown
                )
            }
        }
    }

    class StoryComparator :
        DiffUtil.ItemCallback<Story>() {
        override fun areItemsTheSame(
            oldItem: Story,
            newItem: Story
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Story,
            newItem: Story
        ): Boolean {
            return oldItem.recordUrl == newItem.recordUrl
                    || oldItem.isLiked == newItem.isLiked
                    || oldItem.isBookmarked == newItem.isBookmarked
                    || oldItem.isDownloaded == newItem.isDownloaded
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoryViewHolder {
        return StoryViewHolder.create(
            parent,
            playingStory,
            onStoryPlay,
            onOptionClick,
            shouldPinLocationBeShown
        )
    }

    override fun onBindViewHolder(
        holder: StoryViewHolder,
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
