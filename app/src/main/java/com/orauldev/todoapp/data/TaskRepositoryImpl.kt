package com.orauldev.todoapp.data

import com.orauldev.todoapp.data.source.local.TaskDao
import com.orauldev.todoapp.data.source.network.NetworkDataSource
import com.orauldev.todoapp.di.DefaultDispatcher
import com.orauldev.todoapp.di.IoScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val localDataSource: TaskDao,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoScope private val ioScope: CoroutineScope
) : TaskRepository {

    override fun getTasks(): Flow<List<Task>> {
        return localDataSource.getAllTasks().map { tasks ->
            withContext(defaultDispatcher) {
                tasks.toExternal()
            }
        }
    }

    override suspend fun refresh() {
        ioScope.launch {
            networkDataSource.loadTasks().forEach { networkTask ->
                localDataSource.upsert(networkTask.toLocal())
            }
        }
    }

    override suspend fun createTask(title: String, description: String) {
        ioScope.launch {
            localDataSource.upsert(
                Task(
                    title = title,
                    description = description,
                    id = UUID.randomUUID().toString()
                ).toLocal()
            )
        }
    }

    override suspend fun updateTask(task: Task) {
        ioScope.launch {
            localDataSource.upsert(task.toLocal())
        }
    }

    override suspend fun completeTask(task: Task) {
        ioScope.launch {
            localDataSource.upsert(task.copy(isCompleted = true).toLocal())
        }
    }

    override suspend fun activateTask(task: Task) {
        ioScope.launch {
            localDataSource.upsert(task.copy(isCompleted = false).toLocal())
        }
    }

    override suspend fun clearCompletedTasks() {
        ioScope.launch { localDataSource.deleteAllCompletedTasks() }
    }

    override suspend fun deleteTask(taskId: String) {
        ioScope.launch {
            localDataSource.deleteTaskById(taskId)
        }
    }
}