package com.autio.android_app.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.model.PlaylistOption
import com.autio.android_app.data.model.PlaylistOptionClickListener
import com.autio.android_app.data.model.StoryOption
import com.autio.android_app.data.model.StoryOptionClickListener
import com.autio.android_app.data.model.story.DownloadedStory
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.ui.view.usecases.home.adapter.PlaylistOptionsAdapter
import com.autio.android_app.ui.view.usecases.home.adapter.StoryOptionsAdapter

fun showPlaylistOptions(
    context: Context,
    root: ViewGroup,
    anchor: View,
    options: List<PlaylistOption>,
    onOptionClicked: ((PlaylistOption) -> Unit)?,
    onDismiss: (() -> Unit)? = null
) {
    val inflater =
        context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater
    val view =
        inflater.inflate(
            R.layout.list_popup_window,
            root,
            false
        )
    val recyclerView =
        view.findViewById<RecyclerView>(
            R.id.rvWindowPopupList
        )
    val popup =
        PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    val storyOptionsAdapter =
        PlaylistOptionsAdapter(
            options,
            object :
                PlaylistOptionClickListener {
                override fun onOptionClick(
                    option: PlaylistOption
                ) {
                    popup.dismiss()
                    onOptionClicked?.invoke(
                        option
                    )
                }
            }
        )
    recyclerView.layoutManager =
        NonScrollableLinearLayout(
            context
        )
    recyclerView.adapter =
        storyOptionsAdapter
    popup.isOutsideTouchable =
        true
    popup.isFocusable =
        true
    popup.setOnDismissListener {
        onDismiss?.invoke()
    }
    popup.showAsDropDown(
        anchor,
        -325,
        0
    )
}

fun showStoryOptions(
    context: Context,
    root: ViewGroup,
    anchor: View,
    story: Story,
    options: List<StoryOption>,
    onOptionClick: ((StoryOption, Story) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val inflater =
        context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater
    val view =
        inflater.inflate(
            R.layout.list_popup_window,
            root,
            false
        )
    val popup =
        PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
    val recyclerView =
        view.findViewById<RecyclerView>(
            R.id.rvWindowPopupList
        )
    val storyOptionsAdapter =
        StoryOptionsAdapter(
            story,
            options,
            object :
                StoryOptionClickListener<Story> {
                override fun onItemClick(
                    option: StoryOption,
                    story: Story
                ) {
                    popup.dismiss()
                    onOptionClick?.invoke(
                        option,
                        story
                    )
                }
            }
        )
    recyclerView.layoutManager =
        NonScrollableLinearLayout(
            context
        )
    recyclerView.adapter =
        storyOptionsAdapter
    popup.setBackgroundDrawable(
        ColorDrawable()
    )
    popup.isOutsideTouchable =
        true
    popup.isFocusable =
        true
    popup.setOnDismissListener {
        onDismiss?.invoke()
    }
    popup.showAsDropDown(
        anchor,
        -200,
        0
    )
}

fun showStoryOptions(
    context: Context,
    root: ViewGroup,
    anchor: View,
    story: DownloadedStory,
    options: List<StoryOption>,
    onOptionClick: ((StoryOption, DownloadedStory) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val inflater =
        context.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater
    val view =
        inflater.inflate(
            R.layout.list_popup_window,
            root,
            false
        )
    val popup =
        PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
    val recyclerView =
        view.findViewById<RecyclerView>(
            R.id.rvWindowPopupList
        )
    val storyOptionsAdapter =
        StoryOptionsAdapter(
            story,
            options,
            object :
                StoryOptionClickListener<DownloadedStory> {
                override fun onItemClick(
                    option: StoryOption,
                    story: DownloadedStory
                ) {
                    popup.dismiss()
                    onOptionClick?.invoke(
                        option,
                        story
                    )
                }
            }
        )
    recyclerView.layoutManager =
        NonScrollableLinearLayout(
            context
        )
    recyclerView.adapter =
        storyOptionsAdapter
    popup.setBackgroundDrawable(
        ColorDrawable()
    )
    popup.isOutsideTouchable =
        true
    popup.isFocusable =
        true
    popup.setOnDismissListener {
        onDismiss?.invoke()
    }
    popup.showAsDropDown(
        anchor,
        -200,
        0
    )
}

class NonScrollableLinearLayout(
    context: Context
) : LinearLayoutManager(
    context
) {
    override fun canScrollVertically(): Boolean {
        return false
    }
}