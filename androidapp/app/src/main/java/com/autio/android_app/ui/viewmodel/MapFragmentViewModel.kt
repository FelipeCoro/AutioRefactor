package com.autio.android_app.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.autio.android_app.domain.mappers.toModel
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.player.EMPTY_PLAYBACK_STATE
import com.autio.android_app.player.MediaItemData
import com.autio.android_app.player.PlayerServiceConnection
import com.autio.android_app.ui.stories.models.Story
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapFragmentViewModel @Inject constructor(
    private val mediaId: String,
    playerServiceConnection: PlayerServiceConnection,
    private val autioRepository: AutioRepository,
) : ViewModel() {

    private val _storiesInScreen = MutableLiveData<List<Story>>()
    val storiesInScreen: LiveData<List<Story>> = _storiesInScreen

    private val _isListDisplaying = MutableLiveData(
        false
    )
    val isListDisplaying: LiveData<Boolean> = _isListDisplaying

    private val subscriptionCallback = object : SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>
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
     * There's three things watched that will cause the
     * [LiveData] exposed from this class to be updated
     *
     * [PlayerServiceConnection.playbackState] changes state based on the playback state of
     * the player, which can change the [MediaItemData.playbackRes] in the list
     *
     * [PlayerServiceConnection.nowPlaying] changes based on the item that's being played,
     * which can also change the [MediaItemData.playbackRes] in the list
     */
    private val playerServiceConnection = playerServiceConnection.also {
        it.subscribe(
            mediaId, subscriptionCallback
        )

        it.playbackState.observeForever(
            playbackStateObserver
        )
//        it.nowPlaying.observeForever(mediaMetadataObserver)
    }

    /**
     * Since we use [LiveData.observeForever], it should be called [LiveData.removeObserver]
     * here to prevent leaking resources when the [ViewModel] is not longer in use
     */
    override fun onCleared() {
        super.onCleared()

        // Remove permanent observers from the PlayerServiceConnection
        playerServiceConnection.playbackState.removeObserver(
            playbackStateObserver
        )
//        playerServiceConnection.nowPlaying.removeObserver(mediaMetadataObserver)

        // Unsubscribe media ID being watched
        playerServiceConnection.unsubscribe(
            mediaId, subscriptionCallback
        )
    }

    fun displayListForStoriesInScreen() {
        _isListDisplaying.postValue(
            !(_isListDisplaying.value ?: false)
        )
    }

    suspend fun changeLatLngBounds(
        latLngBounds: LatLngBounds
    ) {
        viewModelScope.launch {
            val storiesInBounds = autioRepository.getStoriesInLatLngBoundaries(
                latLngBounds.southwest, latLngBounds.northeast
            )
            _storiesInScreen.postValue(
                storiesInBounds.map { it.toModel() }
            )
        }
    }

    fun fetchRecordsOfStories(
        userId: Int, apiToken: String
    ) {
        viewModelScope.launch {
            _storiesInScreen.value?.let { stories ->
                if (stories.isEmpty()) return@launch
                val storiesWithoutRecords = stories.filter { it.recordUrl.isEmpty() }
                if (storiesWithoutRecords.isNotEmpty()) {

                    autioRepository.getStoriesByIds(userId, apiToken, storiesWithoutRecords)

                }
            }
        }
    }
}
