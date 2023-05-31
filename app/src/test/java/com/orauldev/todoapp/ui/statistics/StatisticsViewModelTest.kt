package com.orauldev.todoapp.ui.statistics

import com.orauldev.todoapp.MainCoroutineRule
import com.orauldev.todoapp.R
import com.orauldev.todoapp.data.Task
import com.orauldev.todoapp.data.TaskRepository
import com.orauldev.todoapp.util.Async
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class StatisticsViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @RelaxedMockK
    lateinit var taskRepository: TaskRepository

    private lateinit var viewModel: StatisticsViewModel

    @Before
    fun setUp() {

        viewModel = StatisticsViewModel(taskRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `uiState should emit correct values when tasksAsync is Async Success`() = runTest {
        val tasks = listOf(
            Task("Task 1", "Description 1", false, "1"),
            Task("Task 2", "Description 2", true, "2")
        )

        every { taskRepository.getTasks() } returns flow { emit(tasks) }

        val job = launch {
            viewModel.uiState.collect { uiState ->
                assertEquals(false, uiState.isEmpty)
                assertEquals(false, uiState.isLoading)
                assertEquals(false, uiState.hasError)
                assertEquals(50f, uiState.activeTasksPercent)
                assertEquals(50f, uiState.completedTasksPercent)
            }
        }

        job.cancel()
    }

    @Test
    fun `uiState should emit correct values when tasksAsync is Loading`() = runTest {
        every { taskRepository.getTasks() } returns flow { emit(emptyList()) }

        val job = launch {
            viewModel.uiState.collect { uiState ->
                assertEquals(true, uiState.isEmpty)
                assertEquals(true, uiState.isLoading)
                assertEquals(false, uiState.hasError)
                assertEquals(0f, uiState.activeTasksPercent)
                assertEquals(0f, uiState.completedTasksPercent)
            }
        }
        job.cancel()
    }

    @Test
    fun `uiState should emit correct values when tasksAsync is Error`() = runTest {
        every { taskRepository.getTasks() } returns flow { throw Exception() }

        val job = launch {
            viewModel.uiState.collect { uiState ->
                assertEquals(true, uiState.isEmpty)
                assertEquals(false, uiState.isLoading)
                assertEquals(true, uiState.hasError)
                assertEquals(0f, uiState.activeTasksPercent)
                assertEquals(0f, uiState.completedTasksPercent)
            }
        }
        job.cancel()
    }
}