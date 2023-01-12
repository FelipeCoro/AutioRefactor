package com.autio.android_app.data.model

interface OptionClickListener<T> {
    fun onItemClick(option: StoryOption, story: T)
}