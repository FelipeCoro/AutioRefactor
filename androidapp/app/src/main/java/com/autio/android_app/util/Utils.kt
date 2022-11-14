package com.autio.android_app.util

import android.content.Context
import android.widget.EditText
import android.widget.Toast

object Utils {

    fun checkEmptyField(
        etText: EditText
    ): Boolean {
        return etText.text.toString()
            .trim()
            .isEmpty()
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
        context: Context
    ) {
        Toast.makeText(
            context,
            "Ha ocurrido un error",
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


}