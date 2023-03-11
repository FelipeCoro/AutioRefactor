package com.autio.android_app.util.resources

import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest

class DeepLinkingActions {
    companion object {
        const val LoginFragmentDeepLinkingAction: String = "login"
        const val SubscribeFragmentDeepLinkingAction = "subscribe"
    }
}

fun getDeepLinkingNavigationRequest(deepLinkAction: String): NavDeepLinkRequest {
    val action = "android-app://navigation.autio.app/$deepLinkAction"
    return NavDeepLinkRequest.Builder.fromUri(action.toUri()).build()
}