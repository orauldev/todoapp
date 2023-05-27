package com.orauldev.todoapp.di

import android.content.Context
import androidx.room.Room
import com.orauldev.todoapp.data.TaskRepository
import com.orauldev.todoapp.data.TaskRepositoryImpl
import com.orauldev.todoapp.data.source.local.AppDatabase
import com.orauldev.todoapp.data.source.local.TaskDao
import com.orauldev.todoapp.data.source.network.NetworkDataSource
import com.orauldev.todoapp.data.source.network.NetworkDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindTaskRepository(repository: TaskRepositoryImpl): TaskRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Singleton
    @Binds
    abstract fun bindNetworkDataSource(dataSource: NetworkDataSourceImpl): NetworkDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext.applicationContext,
            AppDatabase::class.java,
            "todoapp.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTasksDao(appDatabase: AppDatabase): TaskDao = appDatabase.taskDao()
}