package com.autio.android_app.util

import androidx.fragment.app.Fragment
import com.autio.android_app.ui.stories.BottomNavigation

val Fragment.bottomNavigationActivity
    get() = activity as BottomNavigation?


fun Fragment.showFeedbackSnackBar(message: String) {
    bottomNavigationActivity?.showFeedbackSnackBar(message)
}

fun Fragment.getFloatingComponentHeight():Float?{
   return  bottomNavigationActivity?.getFloatingComponentHeight()
}

