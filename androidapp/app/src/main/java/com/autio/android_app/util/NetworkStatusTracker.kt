package com.autio.android_app.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

sealed class NetworkStatus {
    object Available :
        NetworkStatus()

    object Unavailable :
        NetworkStatus()
}

class NetworkStatusTracker(
    context: Context
) {
    private val connectivityManager =
        context.getSystemService(
            CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    val networkStatus =
        callbackFlow {
            val networkStatusCallback =
                object :
                    NetworkCallback() {
                    override fun onUnavailable() {
                        super.onUnavailable()
                        trySend(
                            NetworkStatus.Unavailable
                        ).isSuccess
                    }

                    override fun onAvailable(
                        network: Network
                    ) {
                        super.onAvailable(network)
                        trySend(
                            NetworkStatus.Available
                        ).isSuccess
                    }

                    override fun onLost(
                        network: Network
                    ) {
                        super.onLost(network)
                        trySend(
                            NetworkStatus.Unavailable
                        ).isSuccess
                    }
                }

            val request =
                NetworkRequest.Builder()
                    .addCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    )
                    .build()
            connectivityManager.registerNetworkCallback(
                request,
                networkStatusCallback
            )

            awaitClose {
                connectivityManager.unregisterNetworkCallback(
                    networkStatusCallback
                )
            }
        }
            .distinctUntilChanged()
}

@FlowPreview
inline fun <Result> Flow<NetworkStatus>.map(
    crossinline onUnavailable: suspend () -> Result,
    crossinline onAvailable: suspend () -> Result,
): Flow<Result> =
    map { status ->
        when (status) {
            NetworkStatus.Unavailable -> onUnavailable()
            NetworkStatus.Available -> onAvailable()
        }
    }

@FlowPreview
inline fun <Result> Flow<NetworkStatus>.flatMap(
    crossinline onUnavailable: suspend () -> Flow<Result>,
    crossinline onAvailable: suspend () -> Flow<Result>,
): Flow<Result> =
    flatMapConcat { status ->
        when (status) {
            NetworkStatus.Unavailable -> onUnavailable()
            NetworkStatus.Available -> onAvailable()
        }
    }