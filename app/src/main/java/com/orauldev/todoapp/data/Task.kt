package com.orauldev.todoapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val id: String,
) : Parcelable {
    val isActive
        get() = !isCompleted
    val isEmpty
        get() = title.isEmpty() || description.isEmpty()
}
