package com.orauldev.todoapp.data

import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun getTasks(): Flow<List<Task>>

    suspend fun refresh()

    suspend fun createTask(title: String, description: String)

    suspend fun updateTask(task: Task)

    suspend fun completeTask(task: Task)

    suspend fun activateTask(task: Task)

    suspend fun clearCompletedTasks()

    suspend fun deleteTask(taskId: String)
}