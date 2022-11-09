package com.autio.android_app.util

import android.content.Context
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class SwipeGesture(context: Context?): ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or
            ItemTouchHelper.START or
            ItemTouchHelper.DOWN or
            ItemTouchHelper.END,
    0){

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }
}