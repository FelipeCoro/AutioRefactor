package com.autio.android_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.autio.android_app.util.NetworkStatusTracker
import com.autio.android_app.util.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview

sealed class MyState {
    object Fetched :
        MyState()

    object Error :
        MyState()
}

class NetworkStatusViewModel(
    networkStatusTracker: NetworkStatusTracker
) : ViewModel() {
    @OptIn(
        FlowPreview::class
    )
    val state =
        networkStatusTracker.networkStatus
            .map(
                onAvailable = { MyState.Fetched },
                onUnavailable = { MyState.Error }
            )
            .asLiveData(
                Dispatchers.IO
            )

    class Factory(
        private val networkStatusTracker: NetworkStatusTracker
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress(
            "unchecked_cast"
        )
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            return NetworkStatusViewModel(
                networkStatusTracker
            ) as T
        }
    }
}