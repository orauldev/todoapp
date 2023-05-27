package com.orauldev.todoapp.ui.addtask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.orauldev.todoapp.R
import com.orauldev.todoapp.databinding.FragmentAddTaskBinding
import com.orauldev.todoapp.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddTaskFragment : Fragment() {
    private val binding by lazy { FragmentAddTaskBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<AddTaskViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        setObserver()
    }

    private fun setViews() {
        binding.fabSaveTask.setOnClickListener {
            viewModel.saveTask(
                title = binding.addTaskTitleEditText.text.toString(),
                description = binding.addTaskDescriptionEditText.text.toString()
            )
        }
    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { uiState ->

                    uiState.userMessage?.let { message ->
                        binding.root.showSnackbar(message)

                        if (uiState.isTaskSaved) {
                            val args = Bundle().apply { putInt(ARGS_KEY, message) }
                            findNavController().navigate(R.id.action_add_task_to_tasks, args)
                        }
                    }
                }

            }
        }
    }

    companion object {
        const val ARGS_KEY = "args_key"
    }
}