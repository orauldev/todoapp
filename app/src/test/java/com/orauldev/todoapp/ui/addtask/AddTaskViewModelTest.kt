package com.orauldev.todoapp.ui.addtask

import com.orauldev.todoapp.MainCoroutineRule
import com.orauldev.todoapp.R
import com.orauldev.todoapp.data.TaskRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
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
class AddTaskViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @RelaxedMockK
    lateinit var taskRepository: TaskRepository

    private lateinit var viewModel: AddTaskViewModel

    @Before
    fun setUp() {

        viewModel = AddTaskViewModel(taskRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `saveTask should update uiState with empty task message when title or description is empty`() = runTest {
        val title = ""
        val description = "Task description"

        viewModel.saveTask(title, description)

        val uiState = viewModel.uiState.value
        assertEquals(R.string.empty_task_message, uiState.userMessage)
        assertFalse(uiState.isTaskSaved)
    }

    @Test
    fun `saveTask should create new task and update uiState with success message`() = runTest {
        val title = "Task title"
        val description = "Task description"

        coEvery { taskRepository.createTask(title, description) } just runs

        viewModel.saveTask(title, description)

        coVerify { taskRepository.createTask(title, description) }

        val uiState = viewModel.uiState.value
        assertEquals(R.string.successfully_saved_task_message, uiState.userMessage)
        assertTrue(uiState.isTaskSaved)
    }
}