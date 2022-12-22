package com.autio.android_app.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Handler
import android.os.Looper
import android.widget.ImageView

// Card flip vertically
fun ImageView.animateFlip() {
    val randomDelay =
        (0..16000).random()
    Handler(
        Looper.getMainLooper()
    ).postDelayed(
        {
            this.rotationY =
                0f
            this.animate()
                .rotationY(
                    90f
                )
                .setListener(
                    object :
                        AnimatorListenerAdapter() {
                        override fun onAnimationEnd(
                            animation: Animator
                        ) {
                            this@animateFlip.rotationY =
                                270f
                            this@animateFlip.animate()
                                .rotationY(
                                    360f
                                )
                                .setListener(
                                    null
                                )
                        }
                    })
        },
        randomDelay.toLong()
    )
}