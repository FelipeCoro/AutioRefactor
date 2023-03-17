package com.autio.android_app.data.api.model

import com.autio.android_app.R

enum class StoryOption {
    DELETE {
        override val option: MenuOption
            get() = MenuOption(
                "Remove",
                R.drawable.ic_close
            )
        override var onClickListener: (() -> Unit)? =
            null
    },
 //  BOOKMARK {
 //      override val option: MenuOption
 //          get() = MenuOption(
 //              "Bookmark",
 //              R.drawable.ic_player_bookmark
 //          )
 //      override var onClickListener: (() -> Unit)? =
 //          null
 //  },
 //  REMOVE_BOOKMARK {
 //      override val option: MenuOption
 //          get() = MenuOption(
 //              "Bookmarked",
 //              R.drawable.ic_player_bookmark_filled
 //          )
 //      override var onClickListener: (() -> Unit)? =
 //          null
 //  },
 //  LIKE {
 //      override val option: MenuOption
 //          get() = MenuOption(
 //              "Like",
 //              R.drawable.ic_heart
 //          )
 //      override var onClickListener: (() -> Unit)? =
 //          null
 //  },
 //  REMOVE_LIKE {
 //      override val option: MenuOption
 //          get() = MenuOption(
 //              "Liked",
 //              R.drawable.ic_heart_filled
 //          )
 //      override var onClickListener: (() -> Unit)? =
 //          null
 //  },
 //  DOWNLOAD {
 //      override val option: MenuOption
 //          get() = MenuOption(
 //              "Download",
 //              R.drawable.ic_download
 //          )
 //      override var onClickListener: (() -> Unit)? =
 //          null
 //  },
 //  REMOVE_DOWNLOAD {
 //      override val option: MenuOption
 //          get() = MenuOption(
 //              "Downloaded",
 //              R.drawable.ic_close
 //          )
 //      override var onClickListener: (() -> Unit)? =
 //          null
 //  },
   DIRECTIONS {
       override val option: MenuOption
           get() = MenuOption(
               "Directions",
               R.drawable.ic_map_selected_light
           )
       override var onClickListener: (() -> Unit)? =
           null
   },
   SHARE {
       override val option: MenuOption
           get() = MenuOption(
               "Share",
               R.drawable.ic_share
           )
       override var onClickListener: (() -> Unit)? =
           null
   };

    abstract val option: MenuOption
    abstract var onClickListener: (() -> Unit)?
}
