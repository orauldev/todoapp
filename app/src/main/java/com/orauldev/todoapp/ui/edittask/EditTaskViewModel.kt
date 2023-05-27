package com.orauldev.todoapp.ui.edittask

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

data class EditTaskUiState(
    val userMessage: Int? = null,
    val isTaskSaved: Boolean = false
)

@HiltViewModel
class EditTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTaskUiState())
    val uiState: StateFlow<EditTaskUiState> get() = _uiState.asStateFlow()

    fun updateTask(task: Task) = viewModelScope.launch {
        if (task.title.isEmpty() || task.description.isEmpty()) {
            _uiState.update {
                it.copy(userMessage = R.string.empty_task_message)
            }
        } else {
            taskRepository.updateTask(task)
            _uiState.update {
                it.copy(
                    userMessage = R.string.successfully_saved_task_message,
                    isTaskSaved = true
                )
            }
        }
    }
}