package com.orauldev.todoapp.util

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.showSnackbar(@StringRes message: Int, anchorView: View? = null) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).run {
        anchorView?.let { setAnchorView(it) }
        show()
    }
}