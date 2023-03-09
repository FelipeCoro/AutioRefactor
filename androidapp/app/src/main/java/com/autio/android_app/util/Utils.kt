package com.autio.android_app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.data.repository.prefs.PrefRepositoryImpl
import com.autio.android_app.ui.stories.BottomNavigation
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject


fun checkEmptyField(
    etText: EditText
): Boolean {
    return etText.text.toString()
        .trim()
        .isEmpty()
}

fun checkEmptyFormFields(
    formFields: Array<EditText>
): Boolean {
    return formFields.all {
        it.text.toString().trim().isEmpty()
    }
}

fun pleaseFillText(context: Context) {
    Toast.makeText(context, "Please enter your information", Toast.LENGTH_SHORT).show()
}

fun showError(context: Context, message: String = "An unexpected error occurred") {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun getDeviceData(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    return if (model.lowercase().startsWith(manufacturer.lowercase())) {
        model.capitalize()
    } else {
        "${manufacturer.capitalize()} $model"
    }
}

fun openUrl(context: Context, route: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(route)
    startActivity(context, intent, null)
}

fun writeEmailToCustomerSupport(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@autio.com"))
        putExtra(Intent.EXTRA_SUBJECT, "Autio Android Customer Support")
        putExtra(
            Intent.EXTRA_TEXT, """                    Device: ${getDeviceData()}
                    Android Version: ${Build.VERSION.SDK_INT}
                    App Version: ${Build.VERSION.RELEASE}
                """.trimIndent()
        )
        startActivity(context, intent, null)
    }
}


fun String.capitalize(): String {
    return replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
        else this.toString()
    }
}

fun shareStory(context: Context, storyId: Int) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT, "https://app.autio.com/stories/$storyId"
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(intent, null)
    context.startActivity(shareIntent)
}

fun openLocationInMapsApp(activity: Activity, latitude: Double, longitude: Double) {
    val intentUri = Uri.parse("geo:$latitude, $longitude")
    val mapIntent = Intent(Intent.ACTION_VIEW, intentUri)
    if (mapIntent.resolveActivity(activity.packageManager) != null) {
        activity.startActivity(mapIntent)
    }
}

fun showPaywall(activity: Activity) {
    (activity as BottomNavigation).showPayWall()
}






