package com.autio.android_app.data.api.model

interface StoryOptionClickListener<T> {
    fun onItemClick(
        option: StoryOption,
        story: T
    )
}
