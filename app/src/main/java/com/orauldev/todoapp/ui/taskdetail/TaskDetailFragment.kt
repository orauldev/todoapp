package com.orauldev.todoapp.ui.taskdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.orauldev.todoapp.databinding.FragmentTaskDetailBinding
import com.orauldev.todoapp.ui.tasks.TasksFragment
import com.orauldev.todoapp.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {

    private val binding by lazy { FragmentTaskDetailBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<TaskDetailViewModel>()
    private lateinit var task: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let { args ->
            task = args.getParcelable(TasksFragment.ARGS_KEY)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_delete -> {
                viewModel.deleteTask(task.id)
                val args = Bundle().apply { putInt(ARGS_KEY, R.string.successfully_deleted_task_message) }
                findNavController().navigate(R.id.action_task_detail_to_tasks, args)
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { uiState ->

                    uiState.userMessage?.let { message ->
                        binding.root.showSnackbar(message)
                    }


                }
            }
        }

        task.let { task ->

            when {
                task.isCompleted -> {
                    binding.completeCheckbox.let { checkBox ->
                        checkBox.isChecked = true
                        checkBox.setOnClickListener {
                            viewModel.completeTask(task, false)
                            checkBox.isChecked = false
                        }
                    }
                }
                task.isActive -> {
                    binding.completeCheckbox.let { checkBox ->
                        checkBox.isChecked = false
                        checkBox.setOnClickListener {
                            viewModel.completeTask(task, true)
                            checkBox.isChecked = true
                        }
                    }
                }
            }

            binding.titleText.text = task.title
            binding.descriptionText.text= task.description
            binding.fabEditTask.setOnClickListener {
                val args = Bundle().apply { putParcelable(ARGS_KEY, task) }
                findNavController().navigate(R.id.action_task_detail_to_edit_task, args)
            }
        }
    }

    companion object {
        const val ARGS_KEY = "args_key"
    }

}