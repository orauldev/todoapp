package com.orauldev.todoapp.ui.tasks

import androidx.lifecycle.SavedStateHandle
import com.orauldev.todoapp.MainCoroutineRule
import com.orauldev.todoapp.R
import com.orauldev.todoapp.data.Task
import com.orauldev.todoapp.data.TaskRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class TasksViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    lateinit var taskRepository: TaskRepository

    @RelaxedMockK
    lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: TasksViewModel

    @ExperimentalCoroutinesApi
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {

        every { savedStateHandle.getStateFlow<String>(any(), any()) } returns MutableStateFlow("")
        every { taskRepository.getTasks() } returns flowOf(emptyList())
        coEvery { taskRepository.refresh() } just runs
        coEvery { taskRepository.completeTask(any()) } just runs
        coEvery { taskRepository.activateTask(any()) } just runs
        coEvery { taskRepository.clearCompletedTasks() } just runs

        viewModel = TasksViewModel(taskRepository, savedStateHandle)
    }

    @After
    fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun `uiState should emit correct values when tasksAsync is Async Loading`() = runTest {
        val loadingState = true

        val job = launch {
            viewModel.uiState.collect {
                val collectedLoading = it.isLoading
                assertEquals(loadingState, collectedLoading)
            }
        }

        job.cancel()
    }

    @Test
    fun `uiState should emit correct values when tasksAsync is Async Error`() = runTest {
        val errorState = R.string.loading_tasks_error

        every { taskRepository.getTasks() } coAnswers { throw Exception() }

        val job = launch {
            viewModel.uiState.collect {
                val collectedMessage = it.userMessage
                assertEquals(errorState, collectedMessage)
            }
        }

        job.cancel()
    }

    @Test
    fun `uiState should emit correct values when tasksAsync is Async Success`() = runTest {
        val tasks = listOf(
            Task("Task 1", "Description 1", false, "1"),
            Task("Task 2", "Description 2", true, "2")
        )

        every { taskRepository.getTasks() } returns flow { emit(tasks) }

        val job = launch {
            viewModel.uiState.collect {
                val collectedTasks = it.items
                assertEquals(tasks, collectedTasks)
            }
        }

        job.cancel()
    }

    @Test
    fun `setFiltering should update savedStateHandle`() {
        val filterType = TasksFilterType.ACTIVE_TASKS

        viewModel.setFiltering(filterType)

        verify { savedStateHandle[TASKS_FILTER_SAVED_STATE_KEY] = filterType }
    }

    @Test
    fun `completeTask should call taskRepository and show correct snackbar message`() = runTest {
        val task = Task("Task 1", "Description 1", false, "1")
        val isChecked = true
        val snackbarMessage = R.string.task_marked_complete
        val viewModelSpy = spyk(viewModel, recordPrivateCalls = true)

        viewModelSpy.completeTask(task, isChecked)

        coVerify { taskRepository.completeTask(task) }
        verify { viewModelSpy["showSnackbarMessage"](snackbarMessage) }
    }

    @Test
    fun `refresh should call taskRepository`() = runTest {
        viewModel.refresh()

        coVerify { taskRepository.refresh() }
    }

    @Test
    fun `snackbarMessageShown should reset userMessage`() {
        viewModel.snackbarMessageShown()

        val userMessage = viewModel.uiState.value.userMessage

        assert(userMessage == null)
    }

    @Test
    fun `clearCompletedTasks should call taskRepository and show correct snackbar message`() =
        runTest {
            val snackbarMessage = R.string.completed_tasks_cleared
            val viewModelSpy = spyk(viewModel, recordPrivateCalls = true)

            viewModelSpy.clearCompletedTasks()

            coVerify { taskRepository.clearCompletedTasks() }
            verify { viewModelSpy["showSnackbarMessage"](snackbarMessage) }
        }

}