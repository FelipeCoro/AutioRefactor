package com.autio.android_app.data.model

interface StoryOptionClickListener<T> {
    fun onItemClick(
        option: StoryOption,
        story: T
    )
}