package com.autio.android_app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.ScrollView
import androidx.recyclerview.widget.RecyclerView

// Extension for animations

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

// Recycler view automatic scroll
fun RecyclerView.setAutomaticScroll(
    scrollFocus: Int = ScrollView.FOCUS_DOWN
) {
    this.addOnItemTouchListener(
        object :
            RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(
                rv: RecyclerView,
                e: MotionEvent
            ): Boolean {
                return true
            }
        })

    val timer =
        object :
            CountDownTimer(
                Long.MAX_VALUE,
                50
            ) {
            override fun onTick(
                millisUntilFinished: Long
            ) {
                this@setAutomaticScroll.scrollBy(
                    0,
                    when (scrollFocus) {
                        ScrollView.FOCUS_UP -> -5
                        ScrollView.FOCUS_DOWN -> 5
                        else -> 0
                    }
                )
            }

            override fun onFinish() {
                TODO(
                    "Not yet implemented"
                )
            }
        }
    timer.start()
}