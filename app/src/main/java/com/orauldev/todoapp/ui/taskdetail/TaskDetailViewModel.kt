package com.orauldev.todoapp.ui.taskdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orauldev.todoapp.R
import com.orauldev.todoapp.data.Task
import com.orauldev.todoapp.data.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailUiState(
    val taskId: String = "",
    val title: String = "",
    val description: String = "",
    val userMessage: Int? = null,
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> get() = _uiState.asStateFlow()


    fun completeTask(task: Task, isChecked: Boolean) = viewModelScope.launch {
        if (isChecked) {
            taskRepository.completeTask(task)
            _uiState.update {
                it.copy(userMessage = R.string.task_marked_complete)
            }
        } else {
            taskRepository.activateTask(task)
            _uiState.update {
                it.copy(userMessage = R.string.task_marked_active)
            }
        }
    }

    fun deleteTask(taskId: String) = viewModelScope.launch {
        taskRepository.deleteTask(taskId)
    }
}