package com.orauldev.todoapp.ui.edittask

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
import com.orauldev.todoapp.data.Task
import com.orauldev.todoapp.databinding.FragmentEditTaskBinding
import com.orauldev.todoapp.ui.taskdetail.TaskDetailFragment
import com.orauldev.todoapp.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditTaskFragment : Fragment() {
    private val binding by lazy { FragmentEditTaskBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<EditTaskViewModel>()
    private lateinit var task: Task
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            task = args.getParcelable(TaskDetailFragment.ARGS_KEY)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { uiState ->

                    uiState.userMessage?.let { message ->
                        binding.root.showSnackbar(message)

                        if (uiState.isTaskSaved) {
                            val args = Bundle().apply { putInt(ARGS_KEY, message) }
                            findNavController().navigate(R.id.action_edit_task_to_tasks, args)
                        }
                    }
                }
            }
        }
    }

    private fun setViews() {
        binding.titleEditTask.setText(task.title)
        binding.descriptionEditTask.setText(task.description)
        binding.fabSaveEditTask.setOnClickListener {
            viewModel.updateTask(
                task.copy(
                    title = binding.titleEditTask.text.toString(),
                    description = binding.descriptionEditTask.text.toString()
                )
            )
        }
    }

    companion object {
        const val ARGS_KEY = "args_key"
    }
}