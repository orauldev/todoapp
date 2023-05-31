package com.orauldev.todoapp.data.source

import com.orauldev.todoapp.MainCoroutineRule
import com.orauldev.todoapp.data.Task
import com.orauldev.todoapp.data.TaskRepositoryImpl
import com.orauldev.todoapp.data.source.local.LocalTask
import com.orauldev.todoapp.data.source.local.TaskDao
import com.orauldev.todoapp.data.source.network.NetworkDataSource
import com.orauldev.todoapp.data.source.network.NetworkTask
import com.orauldev.todoapp.data.toExternal
import com.orauldev.todoapp.data.toLocal
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coJustAwait
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExperimentalCoroutinesApi
class TaskRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var taskRepository: TaskRepositoryImpl

    @MockK
    private lateinit var networkDataSource: NetworkDataSource

    @MockK
    private lateinit var localDataSource: TaskDao

    @Before
    fun setUp() {

        taskRepository = TaskRepositoryImpl(
            networkDataSource,
            localDataSource,
            Dispatchers.Default,
            CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getTasks should return tasks from local data source`() = runTest {
        val localTasks = listOf(
            LocalTask("1", "Task 1", "Desc 1", false),
            LocalTask("2", "Task 2", "Desc 2", true)
        )
        val expectedTasks = localTasks.map { it.toExternal() }
        every { localDataSource.getAllTasks() } returns flow { emit(localTasks) }

        val result = taskRepository.getTasks().toList()

        assert(result.size == 1)
        assert(result.first() == expectedTasks)
    }

    @Test
    fun `refresh should load tasks from network and update local data source`() = runTest {
        val networkTasks = listOf(
            NetworkTask("1", "Task 1", "Desc 1"),
            NetworkTask("2", "Task 2", "Desc 2")
        )

        coEvery { networkDataSource.loadTasks() } returns networkTasks
        coEvery { localDataSource.upsert(any()) } returns Unit

         taskRepository.refresh()

        coVerify(exactly = networkTasks.size) { localDataSource.upsert(any()) }

    }

    @Test
    fun `createTask should insert task into local data source`() = runTest {
        val title = "Task 1"
        val description = "Description 1"

        val captor = slot<LocalTask>()
        coEvery { localDataSource.upsert(capture(captor)) } answers { captor.captured }

        val job = launch {
            taskRepository.createTask(title, description)

            val captured = captor.captured

            assertEquals(title, captured.title)
            assertEquals(description, captured.description)
            assertNotNull(captured.id)
        }

        job.cancel()
    }

    @Test
    fun `updateTask should update task in local data source`() = runTest {
        val task = Task("Task 1", "Desc 1", false, "1")

        coJustAwait { localDataSource.upsert(any()) }

        taskRepository.updateTask(task)

        coVerify(exactly = 1) { localDataSource.upsert(task.toLocal()) }
    }

    @Test
    fun `completeTask should mark task as completed in local data source`() = runTest {
        val task = Task("Task 1", "Desc 1", false, "1")

        coJustAwait { localDataSource.upsert(any()) }

        taskRepository.completeTask(task)

        val completedTask = task.copy(isCompleted = true)

        coVerify(exactly = 1) { localDataSource.upsert(completedTask.toLocal()) }
    }

    @Test
    fun `activateTask should mark task as active in local data source`() = runTest {
        val task = Task("Task 1", "Desc 1", true, "1")

        coJustAwait { localDataSource.upsert(any()) }

        taskRepository.activateTask(task)

        val activeTask = task.copy(isCompleted = false)

        coVerify(exactly = 1) { localDataSource.upsert(activeTask.toLocal()) }
    }

    @Test
    fun `clearCompletedTasks should delete completed tasks from local data source`() = runTest {
        coJustAwait { localDataSource.deleteAllCompletedTasks() }

        taskRepository.clearCompletedTasks()

        coVerify(exactly = 1) { localDataSource.deleteAllCompletedTasks() }
    }

    @Test
    fun `deleteTask should delete task with given ID from local data source`() = runTest {
        val taskId = "1"

        coJustAwait { localDataSource.deleteTaskById(taskId) }

        taskRepository.deleteTask(taskId)

        coVerify(exactly = 1) { localDataSource.deleteTaskById(taskId) }
    }
}