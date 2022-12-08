package com.autio.android_app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
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
            }
        }
    timer.start()
}

//Add underlined text for clickable span
fun TextView.makeLinks(
    vararg links: Pair<String, View.OnClickListener>
) {
    val spannableString =
        SpannableString(
            this.text
        )
    var startIndexOfLink =
        -1
    for (link in links) {
        val clickableSpan =
            object :
                ClickableSpan() {
                override fun updateDrawState(
                    textPaint: TextPaint
                ) {
                    textPaint.isUnderlineText =
                        true
                }

                override fun onClick(
                    view: View
                ) {
                    Selection.setSelection(
                        (view as TextView).text as Spannable,
                        0
                    )
                    view.invalidate()
                    link.second.onClick(
                        view
                    )
                }
            }
        startIndexOfLink =
            this.text.toString()
                .indexOf(
                    link.first,
                    startIndexOfLink + 1
                )
        spannableString.setSpan(
            clickableSpan,
            startIndexOfLink,
            startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance()
    this.setText(
        spannableString,
        TextView.BufferType.SPANNABLE
    )
}