package com.autio.android_app.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.extensions.isPlaying
import com.autio.android_app.player.EMPTY_PLAYBACK_STATE
import com.autio.android_app.player.MediaItemData
import com.autio.android_app.player.NOTHING_PLAYING
import com.autio.android_app.player.PlayerServiceConnection

class MapFragmentViewModel(
    private val mediaId: String,
    playerServiceConnection: PlayerServiceConnection
) : ViewModel() {
    /**
     * Use a backing property so consumers of mediaItems only get a [LiveData] instance so
     * they don't inadvertently modify it
     */
    private val _stories = MutableLiveData<List<Story>>()
    val stories : LiveData<List<Story>> = _stories

    /**
     * Pass the status of the [PlayerServiceConnection.networkFailure] through
     */
    val networkError = Transformations.map(playerServiceConnection.networkFailure) { it }

    private val subscriptionCallback = object : SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
//            val itemsList = children.map { child ->
//                val subtitle = child.description.subtitle ?: ""
//                MediaItemData(
//                    child.mediaId!!,
//                    child.description.title.toString(),
//                    subtitle.toString(),
//                    child.description.iconUri!!,
//                    child.isBrowsable,
//                    getResourceForMediaId(child.mediaId!!)
//                )
//            }
//            _mediaItems.postValue(itemsList)
        }
    }

    /**
     * When the session's [PlaybackStateCompat] changes, the [stories] need to be updated
     * so the correct [MediaItemData.playbackRes] is displayed on the active item
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        val playbackState = it ?: EMPTY_PLAYBACK_STATE
        val currentStory = playerServiceConnection.nowPlaying.value
//        if (currentStory?.id != null) {
//            _mediaItems.postValue(updateState(playbackState, currentStory))
//        }
    }

    /**
     * When the session's [MediaMetadataCompat] changes, the [stories] need to be updated
     * as it means the currently active item has changed. As a result, the new, and potentially
     * old item (if there was one), both need to have their [MediaItemData.playbackRes]
     * changed
     */
    private val mediaMetadataObserver = Observer<MediaMetadataCompat> {
        val playbackState = playerServiceConnection.playbackState.value ?: EMPTY_PLAYBACK_STATE
        val metadata = it ?: NOTHING_PLAYING
//        if (metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI) != null) {
//            _mediaItems.postValue(updateState(playbackState, metadata))
//        }
    }

    /**
     * There's three things watched that will cause the
     * [LiveData] exposed from this class to be updated
     *
     * [subscriptionCallback] is called if/when the children of this
     * ViewModel's [mediaId] changes
     *
     * [PlayerServiceConnection.playbackState] changes state based on the playback state of
     * the player, which can change the [MediaItemData.playbackRes] in the list
     *
     * [PlayerServiceConnection.nowPlaying] changes based on the item that's being played,
     * which can also change the [MediaItemData.playbackRes] in the list
     */
    private val playerServiceConnection = playerServiceConnection.also {
        it.subscribe(mediaId, subscriptionCallback)

        it.playbackState.observeForever(playbackStateObserver)
//        it.nowPlaying.observeForever(mediaMetadataObserver)
    }

    /**
     * Since we use [LiveData.observeForever], it should be called [LiveData.removeObserver]
     * here to prevent leaking resources when the [ViewModel] is not longer in use
     */
    override fun onCleared() {
        super.onCleared()

        // Remove permanent observers from the PlayerServiceConnection
        playerServiceConnection.playbackState.removeObserver(playbackStateObserver)
//        playerServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)

        // Unsubscribe media ID being watched
        playerServiceConnection.unsubscribe(mediaId, subscriptionCallback)
    }

    private fun getResourceForMediaId(mediaId: String): Int {
        val isActive = mediaId == playerServiceConnection.nowPlaying.value?.id
        val isPlaying = playerServiceConnection.playbackState.value?.isPlaying ?: false
        return when {
            !isActive -> NO_RES
            isPlaying -> R.drawable.ic_player_pause
            else -> R.drawable.ic_player_play
        }
    }

//    private fun updateState(
//        playbackState: PlaybackStateCompat,
//        story: Story
//    ) : List<MediaItemData> {
//        val newResId = when (playbackState.isPlaying) {
//            true -> R.drawable.ic_player_pause
//            else -> R.drawable.ic_player_play
//        }

//        return stories.value?.map {
//            val useResId = if (it.mediaId == story.id) newResId else NO_RES
//            it.copy(playbackRes = useResId)
//        } ?: emptyList()
//    }

    class Factory(
        private val mediaId: String,
        private val playerServiceConnection: PlayerServiceConnection
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress(
            "unchecked_cast"
        )
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            return MapFragmentViewModel(mediaId, playerServiceConnection) as T
        }
    }
}

private const val NO_RES = 0