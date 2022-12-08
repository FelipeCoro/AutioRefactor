package com.autio.android_app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object Utils {

    fun checkEmptyField(
        etText: EditText
    ): Boolean {
        return etText.text.toString()
            .trim()
            .isEmpty()
    }

    fun checkEmptyFormFields(
        formFields: Array<EditText>
    ):Boolean{
        return formFields.all {
            it.text.toString().trim().isEmpty()
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

    fun writeEmail(
        context: Context,
        addresses: Array<String>,
        subject: String,
        body: String = ""
    ) {
        val intent =
            Intent(
                Intent.ACTION_SENDTO
            )
        intent.apply {
            Log.d(
                "MAIL",
                "Pong!"
            )
            data =
                Uri.parse(
                    "mailto:"
                )
            putExtra(
                Intent.EXTRA_EMAIL,
                addresses
            )
            putExtra(
                Intent.EXTRA_SUBJECT,
                subject
            )
            putExtra(
                Intent.EXTRA_TEXT,
                body
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
}