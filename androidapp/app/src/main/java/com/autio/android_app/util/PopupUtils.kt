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
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.ui.stories.adapter.PlaylistOptionsAdapter
import com.autio.android_app.ui.stories.adapter.StoryOptionsAdapter
import com.autio.android_app.ui.stories.models.Story

fun showPlaylistOptions(
    context: Context,
    root: ViewGroup,
    anchor: View,
    options: List<com.autio.android_app.data.api.model.PlaylistOption>,
    onOptionClicked: ((com.autio.android_app.data.api.model.PlaylistOption) -> Unit)?,
    onDismiss: (() -> Unit)? = null
) {
    val inflater = context.getSystemService(
        Context.LAYOUT_INFLATER_SERVICE
    ) as LayoutInflater
    val view = inflater.inflate(R.layout.list_popup_window, root, false)
    val recyclerView = view.findViewById<RecyclerView>(R.id.rvWindowPopupList)
    val popup = PopupWindow(
        view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
    val storyOptionsAdapter = PlaylistOptionsAdapter(options)
    { playListOption ->
        popup.dismiss()
        onOptionClicked?.invoke(playListOption)
    }
    recyclerView.layoutManager = NonScrollableLinearLayout(
        context
    )
    recyclerView.adapter = storyOptionsAdapter
    popup.isOutsideTouchable = true
    popup.isFocusable = true
    popup.setOnDismissListener { onDismiss?.invoke() }
    popup.showAsDropDown(anchor, -325, 0)
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
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(
        R.layout.list_popup_window, root, false
    )
    val popup = PopupWindow(
        view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true
    )
    val recyclerView = view.findViewById<RecyclerView>(R.id.rvWindowPopupList)
    val storyOptionsAdapter =
        StoryOptionsAdapter(story, options) { storyOption: StoryOption, story: Story ->
            popup.dismiss()
            onOptionClick?.invoke(storyOption, story)
        }

    recyclerView.layoutManager = NonScrollableLinearLayout(context)
    recyclerView.adapter = storyOptionsAdapter
    popup.setBackgroundDrawable(ColorDrawable())
    popup.isOutsideTouchable = true
    popup.isFocusable = true
    popup.setOnDismissListener {
        onDismiss?.invoke()
    }
    popup.showAsDropDown(anchor, -200, 0)
}

fun showStoryOptions(
    context: Context,
    root: ViewGroup,
    anchor: View,
    story: DownloadedStoryEntity,
    options: List<StoryOption>,
    onOptionClick: ((StoryOption, DownloadedStoryEntity) -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val inflater = context.getSystemService(
        Context.LAYOUT_INFLATER_SERVICE
    ) as LayoutInflater
    val view = inflater.inflate(
        R.layout.list_popup_window, root, false
    )
    val popup = PopupWindow(
        view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true
    )
    val recyclerView = view.findViewById<RecyclerView>(
        R.id.rvWindowPopupList
    )
    val storyOptionsAdapter =
        StoryOptionsAdapter(story, options) { option: StoryOption, story: DownloadedStoryEntity ->
            popup.dismiss()
            onOptionClick?.invoke(option, story)
        }
    recyclerView.layoutManager = NonScrollableLinearLayout(context)
    recyclerView.adapter = storyOptionsAdapter
    popup.setBackgroundDrawable(ColorDrawable())
    popup.isOutsideTouchable = true
    popup.isFocusable = true
    popup.setOnDismissListener {
        onDismiss?.invoke()
    }
    popup.showAsDropDown(anchor, -200, 0)
}

class NonScrollableLinearLayout(context: Context) : LinearLayoutManager(context) {
    override fun canScrollVertically(): Boolean {
        return false
    }
}
