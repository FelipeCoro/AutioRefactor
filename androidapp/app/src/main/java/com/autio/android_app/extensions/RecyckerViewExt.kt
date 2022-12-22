package com.autio.android_app.extensions

import android.os.CountDownTimer
import android.view.MotionEvent
import android.widget.ScrollView
import androidx.recyclerview.widget.RecyclerView

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
            }
        }
    timer.start()
}