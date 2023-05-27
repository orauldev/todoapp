package com.orauldev.todoapp.ui.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orauldev.todoapp.R
import com.orauldev.todoapp.data.Task
import com.orauldev.todoapp.data.TaskRepository
import com.orauldev.todoapp.ui.tasks.TasksFilterType.*
import com.orauldev.todoapp.util.Async
import com.orauldev.todoapp.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TasksUiState(
    val items: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
    val userMessage: Int? = null
)

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _savedFilterType =
        savedStateHandle.getStateFlow(TASKS_FILTER_SAVED_STATE_KEY, ALL_TASKS)

    private val _filterUiInfo = _savedFilterType.map { getFilterUiInfo(it) }.distinctUntilChanged()
    private val _isLoading = MutableStateFlow(false)
    private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _filteredTasksAsync =
        combine(taskRepository.getTasks(), _savedFilterType) { tasks, type ->
            filterTasks(tasks, type)
        }
            .map { Async.Success(it) }
            .catch<Async<List<Task>>> { emit(Async.Error(R.string.loading_tasks_error)) }

    val uiState: StateFlow<TasksUiState> = combine(
        _filterUiInfo, _isLoading, _userMessage, _filteredTasksAsync
    ) { filterUiInfo, isLoading, userMessage, tasksAsync ->
        when (tasksAsync) {
            Async.Loading -> {
                TasksUiState(isLoading = true)
            }

            is Async.Error -> {
                TasksUiState(userMessage = tasksAsync.errorMessage)
            }

            is Async.Success -> {
                TasksUiState(
                    items = tasksAsync.data,
                    filteringUiInfo = filterUiInfo,
                    isLoading = isLoading,
                    userMessage = userMessage
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = WhileUiSubscribed,
            initialValue = TasksUiState(isLoading = true)
        )

    fun setFiltering(filterType: TasksFilterType) {
        savedStateHandle[TASKS_FILTER_SAVED_STATE_KEY] = filterType
    }

    private fun filterTasks(tasks: List<Task>, filteringType: TasksFilterType): List<Task> {
        return when (filteringType) {
            ALL_TASKS -> tasks
            ACTIVE_TASKS -> tasks.filter { it.isActive }
            COMPLETED_TASKS -> tasks.filter { it.isCompleted }
        }
    }

    private fun getFilterUiInfo(filterType: TasksFilterType): FilteringUiInfo =
        when (filterType) {
            ALL_TASKS -> {
                FilteringUiInfo(
                    R.string.label_all, R.string.no_tasks_all,
                    R.drawable.logo_no_fill
                )
            }

            ACTIVE_TASKS -> {
                FilteringUiInfo(
                    R.string.label_active, R.string.no_tasks_active,
                    R.drawable.ic_active
                )
            }

            COMPLETED_TASKS -> {
                FilteringUiInfo(
                    R.string.label_completed, R.string.no_tasks_completed,
                    R.drawable.ic_completed
                )
            }
        }

    fun completeTask(task: Task, isChecked: Boolean) = viewModelScope.launch {
        if (isChecked) {
            taskRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            taskRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
    }

    fun refresh() = viewModelScope.launch {
        taskRepository.refresh()
    }

    private fun showSnackbarMessage(message: Int) {
        _userMessage.value = message
    }

    fun snackbarMessageShown() {
        _userMessage.value = null
    }

    fun clearCompletedTasks() = viewModelScope.launch {
        taskRepository.clearCompletedTasks()
        _filteredTasksAsync.distinctUntilChanged()
        showSnackbarMessage(R.string.completed_tasks_cleared)
    }
}

const val TASKS_FILTER_SAVED_STATE_KEY = "TASKS_FILTER_SAVED_STATE_KEY"

data class FilteringUiInfo(
    val currentFilteringLabel: Int = R.string.label_all,
    val noTasksLabel: Int = R.string.no_tasks_all,
    val noTaskIconRes: Int = R.drawable.logo_no_fill,
)