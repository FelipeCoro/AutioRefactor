package com.autio.android_app.util

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.autio.android_app.R
import com.autio.android_app.ui.stories.BottomNavigation

val Fragment.bottomNavigationActivity
    get() = activity as BottomNavigation?

val Fragment.navController: NavController
    get() = run {
        val navHostFragment =
            bottomNavigationActivity?.supportFragmentManager?.findFragmentById(R.id.main_container) as NavHostFragment
        return navHostFragment.navController
    }

fun Fragment.showFeedbackSnackBar(message: String) {
    bottomNavigationActivity?.showFeedbackSnackBar(message)
}

fun Fragment.getFloatingComponentHeight():Float?{
   return  bottomNavigationActivity?.getFloatingComponentHeight()
}

