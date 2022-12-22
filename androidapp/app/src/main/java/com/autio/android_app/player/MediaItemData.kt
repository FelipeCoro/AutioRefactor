package com.autio.android_app.player

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

data class MediaItemData(
    val mediaId: String,
    val title: String,
    val subtitle: String,
    val artUri: Uri,
    val browsable: Boolean,
    val playbackRes: Int,
) {
    override fun toString(): String {
        return """
            MediaItemData: {
                mediaId: $mediaId,
                title: $title,
                subtitle: $subtitle,
                albumArtUri: $artUri,
                browsable: $browsable,
                playbackRes: $playbackRes
            }
        """.trimIndent()
    }

    companion object {
        const val PLAYBACK_RES_CHANGED =
            1

        val diffCallback =
            object :
                DiffUtil.ItemCallback<MediaItemData>() {
                override fun areItemsTheSame(
                    oldItem: MediaItemData,
                    newItem: MediaItemData
                ) =
                    oldItem.mediaId == newItem.mediaId

                override fun areContentsTheSame(
                    oldItem: MediaItemData,
                    newItem: MediaItemData
                ) =
                    oldItem.mediaId == newItem.mediaId && oldItem.playbackRes == newItem.playbackRes

                override fun getChangePayload(
                    oldItem: MediaItemData,
                    newItem: MediaItemData
                ) =
                    if (oldItem.playbackRes != newItem.playbackRes) {
                        PLAYBACK_RES_CHANGED
                    } else null
            }
    }
}