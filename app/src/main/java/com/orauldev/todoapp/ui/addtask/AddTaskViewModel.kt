package com.orauldev.todoapp.ui.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orauldev.todoapp.R
import com.orauldev.todoapp.data.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTaskUiState(
    val title: String = "",
    val description: String = "",
    val userMessage: Int? = null,
    val isTaskSaved: Boolean = false
)

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    fun saveTask(title: String, description: String) {
        if (title.isEmpty() || description.isEmpty()) {
            _uiState.update {
                it.copy(userMessage = R.string.empty_task_message)
            }
            return
        }
        createNewTask(title, description)
    }

    private fun createNewTask(title: String, description: String) = viewModelScope.launch {
        taskRepository.createTask(title, description)
        _uiState.update {
            it.copy(
                userMessage = R.string.successfully_saved_task_message,
                isTaskSaved = true
            )
        }
    }
}