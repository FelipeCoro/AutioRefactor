package com.autio.android_app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

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
        it.text.toString()
            .trim()
            .isEmpty()
    }
}

fun pleaseFillText(
    context: Context
) {
    Toast.makeText(
        context,
        "Please enter your information",
        Toast.LENGTH_SHORT
    )
        .show()
}

fun showError(
    context: Context,
    message: String = "An unexpected error occurred"
) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT
    )
        .show()
}

fun showToast(
    context: Context,
    message: String
) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_SHORT
    )
        .show()
}

fun getDeviceData(): String {
    val manufacturer =
        Build.MANUFACTURER
    val model =
        Build.MODEL
    return if (model.lowercase()
            .startsWith(
                manufacturer.lowercase()
            )
    ) {
        capitalize(
            model
        )
    } else {
        capitalize(
            manufacturer
        ) + " " + model
    }
}

fun openUrl(
    context: Context,
    route: String
) {
    val intent =
        Intent(
            Intent.ACTION_VIEW
        )
    intent.data =
        Uri.parse(
            route
        )
    startActivity(
        context,
        intent,
        null
    )
}

fun writeEmailToCustomerSupport(
    context: Context
) {
    val intent =
        Intent(
            Intent.ACTION_SENDTO
        )
    intent.apply {
        data =
            Uri.parse(
                "mailto:"
            )
        putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf(
                "support@autio.com"
            )
        )
        putExtra(
            Intent.EXTRA_SUBJECT,
            "Autio Android Customer Support"
        )
        putExtra(
            Intent.EXTRA_TEXT,
            """
                    Device: ${getDeviceData()}
                    Android Version: ${Build.VERSION.SDK_INT}
                    App Version: ${Build.VERSION.RELEASE}
                """.trimIndent()
        )
        startActivity(
            context,
            intent,
            null
        )
    }
}

private fun capitalize(
    s: String?
): String {
    if (s == null || s.isEmpty()) {
        return ""
    }
    val first =
        s[0]
    return if (Character.isUpperCase(
            first
        )
    ) {
        s
    } else {
        first.uppercaseChar()
            .toString() + s.substring(
            1
        )
    }
}

fun getIconFromDrawable(
    drawable: Drawable?
): BitmapDescriptor? {
    if (drawable == null) return null
    val canvas =
        Canvas()
    val bitmap =
        Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    canvas.setBitmap(
        bitmap
    )
    drawable.setBounds(
        0,
        0,
        drawable.intrinsicWidth,
        drawable.intrinsicHeight
    )
    drawable.draw(
        canvas
    )
    return BitmapDescriptorFactory.fromBitmap(
        bitmap
    )
}

fun shareStory(
    context: Context,
    storyId: String
) {
    val intent =
        Intent().apply {
            action =
                Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "https://app.autio.com/stories/$storyId"
            )
            type =
                "text/plain"
        }
    val shareIntent =
        Intent.createChooser(
            intent,
            null
        )
    context.startActivity(
        shareIntent
    )
}

fun openLocationInMapsApp(
    activity: Activity,
    latitude: Double,
    longitude: Double
) {
    val intentUri =
        Uri.parse(
            "geo:$latitude, $longitude"
        )
    val mapIntent =
        Intent(
            Intent.ACTION_VIEW,
            intentUri
        )
    if (mapIntent.resolveActivity(
            activity.packageManager
        ) != null
    ) {
        activity.startActivity(
            mapIntent
        )
    }
}