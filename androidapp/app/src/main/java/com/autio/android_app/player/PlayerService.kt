package com.autio.android_app.player

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.service.media.MediaBrowserService.BrowserRoot.EXTRA_RECENT
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.autio.android_app.R
import com.autio.android_app.data.database.StoryDataBase
import com.autio.android_app.extensions.*
import com.autio.android_app.notifications.MediaNotificationManager
import com.autio.android_app.player.library.BrowseTree
import com.autio.android_app.player.library.BrowseTree.Companion.BROWSABLE_ROOT
import com.autio.android_app.player.library.BrowseTree.Companion.EMPTY_ROOT
import com.autio.android_app.player.library.BrowseTree.Companion.MEDIA_SEARCH_SUPPORTED
import com.autio.android_app.player.library.BrowseTree.Companion.RECENT_ROOT
import com.autio.android_app.player.library.JsonSource
import com.autio.android_app.player.library.StorySource
import com.autio.android_app.util.PackageValidator
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.analytics.PlaybackStatsListener
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector.ALL_PLAYBACK_ACTIONS
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.Util.constrainValue
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * [PlayerService] is the entry point for browsing and playback
 * commands from the app's UI or other like Android Auto and Google Assistant
 *
 * Browsing begins with the method [PlayerService.onGetRoot], and continues in
 * the callback [PlayerService.onLoadChildren]
 */
open class PlayerService :
    MediaBrowserServiceCompat() {

    private lateinit var notificationManager: MediaNotificationManager
    private lateinit var mediaSource: StorySource
    private lateinit var packageValidator: PackageValidator

    // The current player will be an ExoPlayer for local playback
    // For casting, it should be a CastPlayer
    private lateinit var currentPlayer: Player

    private val serviceJob =
        SupervisorJob()
    private val serviceScope =
        CoroutineScope(
            Dispatchers.Main + serviceJob
        )

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private var currentPlaylistItems =
        mutableListOf<MediaMetadataCompat>()
    private var currentMediaItemIndex =
        0

    private lateinit var storage: PersistentStorage

    /**
     * This must be `by lazy` because the source won't initially be ready.
     * See [PlayerService.onLoadChildren] to see where it's accessed (and first
     * constructed).
     */
    private val browseTree: BrowseTree by lazy {
        BrowseTree(
            applicationContext,
            mediaSource
        )
    }

    private var isForegroundService =
        false

    private val autioAttributes =
        AudioAttributes.Builder()
            .setContentType(
                C.AUDIO_CONTENT_TYPE_MUSIC
            )
            .setUsage(
                C.USAGE_MEDIA
            )
            .build()

    private val playerListener =
        PlayerEventListener()

    // Configure ExoPlayer to handle audio focus
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(
            this
        )
            .build()
            .apply {
                setAudioAttributes(
                    autioAttributes,
                    true
                )
                setHandleAudioBecomingNoisy(
                    true
                )
                addListener(
                    playerListener
                )
                addAnalyticsListener(
                    PlaybackStatsListener(
                        true,
                        null
                    )
                )
            }
    }

    /**
     * If Cast is available, create a CastPlayer to handle communication with a Cast session.
     */
    private val castPlayer: CastPlayer? by lazy {
        try {
            val castContext =
                CastContext.getSharedInstance(
                    this
                )
            CastPlayer(
                castContext,
                CastMediaItemConverter()
            ).apply {
                setSessionAvailabilityListener(
                    CastSessionAvailabilityListener()
                )
                addListener(
                    playerListener
                )
            }
        } catch (e: Exception) {
            // We wouldn't normally catch the generic `Exception` however
            // calling `CastContext.getSharedInstance` can throw various exceptions, all of which
            // indicate that Cast is unavailable.
            // Related internal bug b/68009560.
            Log.i(
                TAG,
                "Cast is not available on this device. " +
                        "Exception thrown when attempting to obtain CastContext. " + e.message
            )
            null
        }
    }

    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent =
            packageManager.getLaunchIntentForPackage(
                packageName
            )
                ?.let { sessionIntent ->
                    PendingIntent.getActivity(
                        this,
                        0,
                        sessionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }
        mediaSession =
            MediaSessionCompat(
                this,
                "PlayerService"
            ).apply {
                setFlags(
                    MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
                )
                setSessionActivity(
                    sessionActivityPendingIntent
                )
                isActive =
                    true
            }

        /**
         * In order for [MediaBrowserCompat.ConnectionCallback.onConnected] to be called,
         * a [MediaSessionCompat.Token] needs to be set on the [MediaBrowserServiceCompat]
         *
         * It is possible to wait to set the session token, if required for a specific use-case.
         * However, the token must be set by the time [MediaBrowserServiceCompat.onGetRoot]
         * returns, or the connection will fail silently.
         */
        sessionToken =
            mediaSession.sessionToken

        // Notification manager will use our player and media session
        // to decide when to post notifications. When they are posted
        // or removed, the listener will be called, allowing to promote
        // the service to foreground (required so that the service is
        // not killed ig the main UI is not visible)
        notificationManager =
            MediaNotificationManager(
                this,
                mediaSession.sessionToken,
                PlayerNotificationListener()
            )

        mediaSource =
            JsonSource(
                this
            )

        // ExoPlayer will manage the MediaSession
        mediaSessionConnector =
            MediaSessionConnector(
                mediaSession
            )
        mediaSessionConnector.setPlaybackPreparer(
            PlayerPlaybackPreparer()
        )
        mediaSessionConnector.setEnabledPlaybackActions(
            ALL_PLAYBACK_ACTIONS
        )
        mediaSessionConnector.setQueueNavigator(
            PlayerQueueNavigator(
                mediaSession
            )
        )

        switchToPlayer(
            previousPlayer = null,
            newPlayer = if (castPlayer?.isCastSessionAvailable == true) castPlayer!! else exoPlayer
        )
        notificationManager.showNotificationForPlayer(
            currentPlayer
        )

        packageValidator =
            PackageValidator(
                this,
                R.xml.allowed_media_browser_callers
            )

        storage =
            PersistentStorage.getInstance(
                applicationContext
            )
    }

    override fun onTaskRemoved(
        rootIntent: Intent
    ) {
        saveRecentStoryToStorage()
        super.onTaskRemoved(
            rootIntent
        )
        currentPlayer.apply {
            stop()
            clearMediaItems()
        }
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive =
                false
            release()
        }

        serviceJob.cancel()

        exoPlayer.removeListener(
            playerListener
        )
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        val isKnownCaller =
            packageValidator.isKnownCaller(
                clientPackageName,
                clientUid
            )
        val rootExtras =
            Bundle().apply {
                putBoolean(
                    MEDIA_SEARCH_SUPPORTED,
                    isKnownCaller || browseTree.searchableByUnknownCaller
                )
                putBoolean(
                    CONTENT_STYLE_SUPPORTED,
                    true
                )
                putInt(
                    CONTENT_STYLE_BROWSABLE_HINT,
                    CONTENT_STYLE_GRID
                )
                putInt(
                    CONTENT_STYLE_PLAYABLE_HINT,
                    CONTENT_STYLE_LIST
                )
            }
        if (!isKnownCaller) {
            // Unknown caller. There are two main ways to handle this:
            // 1) Return a root without any content, which still allows the
            // connecting client to issue commands
            // 2) Return `null`, which will cause the system to disconnect
            // the app
            return BrowserRoot(
                EMPTY_ROOT,
                rootExtras
            )
        }

        // By default return the browsable root
        val isRecentRequest =
            rootHints?.getBoolean(
                EXTRA_RECENT
            )
                ?: false
        val browserRootPath =
            if (isRecentRequest) RECENT_ROOT else BROWSABLE_ROOT
        return BrowserRoot(
            browserRootPath,
            rootExtras
        )
    }

    /**
     * Return (via [result]) a list of [MediaItem] that are child
     * items of the provided [parentId]
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        // If caller requests the recent root, return the most recently played story
        if (RECENT_ROOT == parentId) {
            result.sendResult(
                storage.loadRecentStory()
                    ?.let { story ->
                        listOf(
                            story
                        )
                    }
            )
        } else {
            // If media source is ready, results will be set synchronously
            val resultsSent =
                mediaSource.whenReady { successfullyInitialized ->
                    if (successfullyInitialized) {
                        val children =
                            browseTree[parentId]?.map { item ->
                                MediaBrowserCompat.MediaItem(
                                    item.description,
                                    item.flag
                                )
                            }
                        result.sendResult(
                            children
                        )
                    } else {
                        mediaSession.sendSessionEvent(
                            NETWORK_FAILURE,
                            null
                        )
                        result.sendResult(
                            null
                        )
                    }
                }

            // If results are not ready, service must detach them before
            // the method returns. After the source is ready, the lambda
            // above will run, and the caller will be notified that
            // results are ready
            if (!resultsSent) {
                result.detach()
            }
        }
    }

    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        val resultsSent =
            mediaSource.whenReady { successfullyInitialized ->
                if (successfullyInitialized) {
                    val resultsList =
                        mediaSource.search(
                            query,
                            extras
                                ?: Bundle.EMPTY
                        )
                            .map { mediaMetadata ->
                                MediaBrowserCompat.MediaItem(
                                    mediaMetadata.description,
                                    mediaMetadata.flag
                                )
                            }
                    result.sendResult(
                        resultsList
                    )
                }
            }

        if (!resultsSent) {
            result.detach()
        }
    }

    fun addStoryToPlaylist(
        metadata: MediaMetadataCompat
    ) {
        currentPlaylistItems.add(
            metadata
        )
        currentPlayer.addMediaItem(
            metadata.toMediaItem()
        )
    }

    fun removeStoryFromPlaylist(
        metadata: MediaMetadataCompat
    ) {
        if (currentPlaylistItems.contains(
                metadata
            )
        ) {
            currentPlaylistItems.remove(
                metadata
            )
        }
        currentPlayer.setMediaItems(
            currentPlaylistItems.map { it.toMediaItem() })
    }

    /**
     * Load the supplied list of stories and the story to play into
     * current player
     */
    private fun preparePlaylist(
        metadataList: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playWhenReady: Boolean,
        playbackStartPositionMs: Long
    ) {
        // Since the playlist was probably based on some ordering (such as tracks
        // on an album), find which window index to play first so that the story
        // the user actually wants to hear plays first
        val initialWindowIndex =
            if (itemToPlay == null) 0 else metadataList.indexOf(
                itemToPlay
            )
        currentPlaylistItems =
            metadataList.toMutableList()

        currentPlayer.playWhenReady =
            playWhenReady
        currentPlayer.stop()
        // Set playlist and prepare
        currentPlayer.setMediaItems(
            metadataList.map {
                it.toMediaItem()
            },
            initialWindowIndex,
            playbackStartPositionMs
        )
        currentPlayer.prepare()
    }

    private fun switchToPlayer(
        previousPlayer: Player?,
        newPlayer: Player
    ) {
        if (previousPlayer == newPlayer) {
            return
        }
        currentPlayer =
            newPlayer
        if (previousPlayer != null) {
            val playbackState =
                previousPlayer.playbackState
            if (currentPlaylistItems.isEmpty()) {
                // We are joining a playback session. Loading the session from the new player is
                // not supported, so we stop playback.
                currentPlayer.clearMediaItems()
                currentPlayer.stop()
            } else if (playbackState != STATE_IDLE && playbackState != STATE_ENDED) {
                preparePlaylist(
                    metadataList = currentPlaylistItems,
                    itemToPlay = currentPlaylistItems[currentMediaItemIndex],
                    playWhenReady = previousPlayer.playWhenReady,
                    playbackStartPositionMs = previousPlayer.currentPosition
                )
            }
        }
        mediaSessionConnector.setPlayer(
            newPlayer
        )
        previousPlayer?.run {
            stop()
            clearMediaItems()
        }
    }

    private fun saveRecentStoryToStorage() {
        // Obtain the current story before saving the on a
        // separate thread, otherwise the current player may
        // have been unloaded by the time the save routine
        // runs
        if (currentPlaylistItems.isEmpty()) return

        val description =
            currentPlaylistItems[currentMediaItemIndex].description
        val position =
            currentPlayer.currentPosition

        serviceScope.launch {
            storage.saveRecentStory(
                description,
                position
            )
        }
    }

    private inner class CastSessionAvailabilityListener :
        SessionAvailabilityListener {
        /**
         * Called when a Cast session has started and the user wishes to control playback on a
         * remote Cast receiver rather than play audio locally.
         */
        override fun onCastSessionAvailable() {
            switchToPlayer(
                currentPlayer,
                castPlayer!!
            )
        }

        /**
         * Called when a Cast session has ended and the user wishes to control playback locally.
         */
        override fun onCastSessionUnavailable() {
            switchToPlayer(
                currentPlayer,
                exoPlayer
            )
        }
    }

    private inner class PlayerQueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(
        mediaSession
    ) {
        override fun getMediaDescription(
            player: Player,
            windowIndex: Int
        ): MediaDescriptionCompat {
            if (windowIndex < currentPlaylistItems.size) {
                return currentPlaylistItems[windowIndex].description
            }
            return MediaDescriptionCompat.Builder()
                .build()
        }
    }

    private inner class PlayerPlaybackPreparer :
        MediaSessionConnector.PlaybackPreparer {
        override fun getSupportedPrepareActions() =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED

        override fun onPrepare(
            playWhenReady: Boolean
        ) {
            val recentStory =
                storage.loadRecentStory()
                    ?: return
            onPrepareFromMediaId(
                recentStory.mediaId!!,
                playWhenReady,
                recentStory.description.extras
            )
        }

        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            serviceScope.launch {
                val itemToPlay =
                    StoryDataBase.getInstance(
                        this@PlayerService,
                        CoroutineScope(
                            SupervisorJob()
                        )
                    )
                        .storyDao()
                        .getStoryById(
                            mediaId
                        )
                if (itemToPlay == null) {
                    Log.w(
                        TAG,
                        "Content not found: MediaID=$mediaId"
                    )
                } else {
                    val itemMetadata =
                        MediaMetadataCompat.Builder()
                            .from(
                                itemToPlay
                            )
                            .build()
                    val playbackStartPositionMs =
                        extras?.getLong(
                            MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS,
                            C.TIME_UNSET
                        )
                            ?: C.TIME_UNSET
                    preparePlaylist(
                        buildPlaylist(
                            itemMetadata
                        ),
                        itemMetadata,
                        playWhenReady,
                        playbackStartPositionMs
                    )
                }
            }
        }

        // Method for Google Assistant for responding to requests
        override fun onPrepareFromSearch(
            query: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            mediaSource.whenReady {
                val metadataList =
                    mediaSource.search(
                        query,
                        extras
                            ?: Bundle.EMPTY
                    )
                if (metadataList.isNotEmpty()) {
                    preparePlaylist(
                        metadataList,
                        metadataList[0],
                        playWhenReady,
                        playbackStartPositionMs = C.TIME_UNSET
                    )
                }
            }
        }

        override fun onPrepareFromUri(
            uri: Uri,
            playWhenReady: Boolean,
            extras: Bundle?
        ) =
            Unit

        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ) =
            false

        /**
         * Builds a playlist
         *
         * @param item Item to base the playlist on
         * @return a [List] of [MediaMetadataCompat] objects representing a playlist
         */
        private fun buildPlaylist(
            item: MediaMetadataCompat
        ): List<MediaMetadataCompat> {
            val playlist =
                mediaSource.filter { it.album == item.album }
                    .sortedBy { it.trackNumber }
            return playlist.ifEmpty {
                listOf(
                    item
                )
            }
        }
    }

    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(
                        applicationContext,
                        this@PlayerService.javaClass
                    )
                )

                startForeground(
                    notificationId,
                    notification
                )
                isForegroundService =
                    true
            }
        }

        override fun onNotificationCancelled(
            notificationId: Int,
            dismissedByUser: Boolean
        ) {
            stopForeground(
                STOP_FOREGROUND_REMOVE
            )
            isForegroundService =
                false
            stopSelf()
        }
    }

    private inner class PlayerEventListener :
        Listener {

        override fun onPlayWhenReadyChanged(
            playWhenReady: Boolean,
            reason: Int
        ) {
            if (!playWhenReady) {
                // If playback is paused we remove the foreground state which allows the
                // notification to be dismissed. An alternative would be to provide a
                // "close" button in the notification which stops playback and clears
                // the notification.
                stopForeground(
                    STOP_FOREGROUND_DETACH
                )
                isForegroundService =
                    false
            }
        }

        override fun onPlaybackStateChanged(
            playbackState: Int
        ) {
            when (playbackState) {
                STATE_BUFFERING,
                STATE_READY -> {
                    notificationManager.showNotificationForPlayer(
                        currentPlayer
                    )
                    if (playbackState == STATE_READY) {
                        // When playing/paused save the current media item in persistent
                        // storage so that playback can be resumed between device reboots.
                        // Search for "media resumption" for more information.
                        saveRecentStoryToStorage()
                    }
                }
                else -> {
                    notificationManager.hideNotification()
                }
            }
        }

        override fun onEvents(
            player: Player,
            events: Events
        ) {
            if (events.containsAny(
                    EVENT_POSITION_DISCONTINUITY,
                    EVENT_MEDIA_ITEM_TRANSITION,
                    EVENT_PLAY_WHEN_READY_CHANGED
                )
            ) {
                currentMediaItemIndex =
                    if (currentPlaylistItems.isNotEmpty()) {
                        constrainValue(
                            player.currentMediaItemIndex,
                            0,
                            currentPlaylistItems.size - 1
                        )
                    } else 0
            }
        }

        override fun onPlayerError(
            error: PlaybackException
        ) {
            var message =
                "Generic error"
            Log.e(
                TAG,
                "Player error: " + error.errorCodeName + " (" + error.errorCode + ")"
            )
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
                || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
            ) {
                message =
                    "Media not available"
            }
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    companion object {
        private val TAG =
            PlayerService::class.simpleName

        /*
         * Session events
         */
        const val NETWORK_FAILURE =
            "autio.audio.travel.guide.media.session.NETWORK_FAILURE"

        /**
         * Content styling constants
         */
        private const val CONTENT_STYLE_BROWSABLE_HINT =
            "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
        private const val CONTENT_STYLE_PLAYABLE_HINT =
            "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
        private const val CONTENT_STYLE_SUPPORTED =
            "android.media.browse.CONTENT_STYLE_SUPPORTED"
        private const val CONTENT_STYLE_LIST =
            1
        private const val CONTENT_STYLE_GRID =
            2

        const val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS =
            "playback_start_position_ms"
    }
}