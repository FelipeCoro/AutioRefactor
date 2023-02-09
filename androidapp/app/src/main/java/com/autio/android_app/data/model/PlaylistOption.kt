package com.autio.android_app.data.model

import com.autio.android_app.R

enum class PlaylistOption {
    DOWNLOAD {
        override val option: MenuOption
            get() = MenuOption(
                "Download All",
                R.drawable.ic_download
            )
        override var onClickListener: (() -> Unit)? =
            null

        override var disabled: Boolean =
            false
    },
    REMOVE {
        override val option: MenuOption
            get() = MenuOption(
                "Remove All",
                R.drawable.ic_thrash
            )
        override var onClickListener: (() -> Unit)? =
            null

        override var disabled: Boolean =
            false
    },
    CLEAR_HISTORY {
        override val option: MenuOption
            get() = MenuOption(
                "Clear History",
                R.drawable.ic_thrash
            )
        override var onClickListener: (() -> Unit)? =
            null

        override var disabled: Boolean =
            false
    };

    abstract val option: MenuOption
    abstract var onClickListener: (() -> Unit)?
    abstract var disabled: Boolean
}