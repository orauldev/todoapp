package com.orauldev.todoapp.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocalTask::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}