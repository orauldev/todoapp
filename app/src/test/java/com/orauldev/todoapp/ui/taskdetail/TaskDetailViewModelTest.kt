package com.orauldev.todoapp.ui.taskdetail

import com.orauldev.todoapp.MainCoroutineRule
import com.orauldev.todoapp.R
import com.orauldev.todoapp.data.Task
import com.orauldev.todoapp.data.TaskRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class TaskDetailViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @RelaxedMockK
    lateinit var taskRepository: TaskRepository

    private lateinit var viewModel: TaskDetailViewModel

    @Before
    fun setUp() {

        viewModel = TaskDetailViewModel(taskRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `completeTask should complete task and update uiState with complete message`() = runTest {
        val task = Task("Task 1", "Description 1", false, "1")
        val isChecked = true

        coEvery { taskRepository.completeTask(task) } just runs

        viewModel.completeTask(task, isChecked)

        coVerify { taskRepository.completeTask(task) }

        val uiState = viewModel.uiState.value
        assertEquals(R.string.task_marked_complete, uiState.userMessage)
    }

    @Test
    fun `completeTask should activate task and update uiState with active message`() = runTest {
        val task = Task("Task 1", "Description 1", true, "1")
        val isChecked = false

        coEvery { taskRepository.activateTask(task) } just runs

        viewModel.completeTask(task, isChecked)

        coVerify { taskRepository.activateTask(task) }

        val uiState = viewModel.uiState.value
        assertEquals(R.string.task_marked_active, uiState.userMessage)
    }

    @Test
    fun `deleteTask should delete task`() = runTest {
        val taskId = "1"

        coEvery { taskRepository.deleteTask(taskId) } just runs

        viewModel.deleteTask(taskId)

        coVerify { taskRepository.deleteTask(taskId) }
    }
}