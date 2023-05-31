package com.orauldev.todoapp.ui.edittask

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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class EditTaskViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @RelaxedMockK
    lateinit var taskRepository: TaskRepository

    private lateinit var viewModel: EditTaskViewModel

    @Before
    fun setUp() {

        viewModel = EditTaskViewModel(taskRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `updateTask should update task and update uiState with success message`() = runTest {
        val task = Task("Task 1", "Description 1", false, "1")

        coEvery { taskRepository.updateTask(task) } just runs

        viewModel.updateTask(task)

        coVerify { taskRepository.updateTask(task) }

        val uiState = viewModel.uiState.value
        assertEquals(R.string.successfully_saved_task_message, uiState.userMessage)
        assertTrue(uiState.isTaskSaved)
    }

    @Test
    fun `updateTask should update uiState with empty task message when task title is empty`() = runTest {
        val task = Task("", "Description 1", false, "1")

        viewModel.updateTask(task)

        val uiState = viewModel.uiState.value
        assertEquals(R.string.empty_task_message, uiState.userMessage)
        assertFalse(uiState.isTaskSaved)
    }

    @Test
    fun `updateTask should update uiState with empty task message when task description is empty`() = runTest {
        val task = Task("Task 1", "", false, "1")

        viewModel.updateTask(task)

        val uiState = viewModel.uiState.value
        assertEquals(R.string.empty_task_message, uiState.userMessage)
        assertFalse(uiState.isTaskSaved)
    }
}