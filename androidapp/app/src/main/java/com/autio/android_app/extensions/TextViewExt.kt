package com.autio.android_app.extensions

import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView

/**
 * Adds underline to text with a click listener
 */
fun TextView.makeLinks(
    vararg links: Pair<String, (View) -> Unit>, shouldUnderline: Boolean = true
) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.isUnderlineText = shouldUnderline
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.invoke(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(
            link.first, startIndexOfLink + 1
        )
        spannableString.setSpan(
            clickableSpan,
            startIndexOfLink,
            startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod = LinkMovementMethod.getInstance()
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}