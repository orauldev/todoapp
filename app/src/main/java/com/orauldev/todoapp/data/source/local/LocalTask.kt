package com.orauldev.todoapp.data.source.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Internal model used to represent a task stored locally in a Room database. This is used inside
 * the data layer only.
 *
 * See ModelMappingExt.kt for mapping functions used to convert this model to other
 * models.
 */

@Entity(tableName = "tasks")
data class LocalTask(
    @PrimaryKey val id: String,
    var title: String,
    var description: String,
    @ColumnInfo(name = "completed") var isCompleted: Boolean
)
