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
import com.autio.android_app.data.entities.story.DownloadedStory
import com.autio.android_app.data.entities.story.Story
import com.autio.android_app.ui.view.usecases.home.adapter.PlaylistOptionsAdapter
import com.autio.android_app.ui.view.usecases.home.adapter.StoryOptionsAdapter

fun showPlaylistOptions(
    context: Context,
    root: ViewGroup,
    anchor: View,
    options: List<com.autio.android_app.data.api.model.PlaylistOption>,
    onOptionClicked: ((com.autio.android_app.data.api.model.PlaylistOption) -> Unit)?,
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
                com.autio.android_app.data.api.model.PlaylistOptionClickListener {
                override fun onOptionClick(
                    option: com.autio.android_app.data.api.model.PlaylistOption
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
    options: List<com.autio.android_app.data.api.model.StoryOption>,
    onOptionClick: ((com.autio.android_app.data.api.model.StoryOption, Story) -> Unit)? = null,
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
                com.autio.android_app.data.api.model.StoryOptionClickListener<Story> {
                override fun onItemClick(
                    option: com.autio.android_app.data.api.model.StoryOption,
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
    options: List<com.autio.android_app.data.api.model.StoryOption>,
    onOptionClick: ((com.autio.android_app.data.api.model.StoryOption, DownloadedStory) -> Unit)? = null,
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
                com.autio.android_app.data.api.model.StoryOptionClickListener<DownloadedStory> {
                override fun onItemClick(
                    option: com.autio.android_app.data.api.model.StoryOption,
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
