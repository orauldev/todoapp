package com.orauldev.todoapp.ui.tasks

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.orauldev.todoapp.R
import com.orauldev.todoapp.databinding.FragmentTasksBinding
import com.orauldev.todoapp.ui.addtask.AddTaskFragment
import com.orauldev.todoapp.ui.edittask.EditTaskFragment
import com.orauldev.todoapp.ui.taskdetail.TaskDetailFragment
import com.orauldev.todoapp.util.gone
import com.orauldev.todoapp.util.showSnackbar
import com.orauldev.todoapp.util.visible

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment() {
    private val binding by lazy { FragmentTasksBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<TasksViewModel>()
    private val adapter by lazy { TasksAdapter ({ task, isChecked ->
        viewModel.completeTask(task, isChecked)
    }, { task ->
        val args = Bundle().apply { putParcelable(ARGS_KEY, task) }
        findNavController().navigate(R.id.action_tasks_to_task_detail, args)
    }) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_filter -> { showFilteringPopUpMenu() ; true }
            R.id.menu_clear -> { viewModel.clearCompletedTasks() ; true }
            R.id.menu_refresh -> { viewModel.refresh() ; true }
            else -> false
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        setObserver()
        getArgs()
    }

    private fun showFilteringPopUpMenu() {
        val view = activity?.findViewById<View>(R.id.menu_filter) ?: return
        PopupMenu(requireContext(), view).run {
            menuInflater.inflate(R.menu.filter_tasks, menu)

            val textColor = ContextCompat.getColor(requireContext(), R.color.colorTextPrimary)

            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                val spannableString = SpannableString(menuItem.title)
                spannableString.setSpan(ForegroundColorSpan(textColor), 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                menuItem.title = spannableString
            }

            setOnMenuItemClickListener {
                viewModel.setFiltering(
                    when (it.itemId) {
                        R.id.active -> TasksFilterType.ACTIVE_TASKS
                        R.id.completed -> TasksFilterType.COMPLETED_TASKS
                        else -> TasksFilterType.ALL_TASKS
                    }
                )
                true
            }
            show()
        }
    }

    private fun setViews() {
        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_tasks_to_add_tasks)
        }
    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->

                    binding.progress.isVisible = uiState.isLoading

                    uiState.userMessage?.let { message ->
                        binding.root.showSnackbar(message, binding.fabAddTask)
                        viewModel.snackbarMessageShown()
                    }

                    binding.recyclerviewTasks.adapter = adapter

                    if (uiState.items.isEmpty()) {
                        binding.noTasksIcon.let {
                            it.visible()
                            it.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireActivity(),
                                    uiState.filteringUiInfo.noTaskIconRes
                                )
                            )
                        }
                        binding.noTasksText.let {
                            it.visible()
                            it.text = getString(uiState.filteringUiInfo.noTasksLabel)
                        }
                        binding.filteringText.gone()
                        adapter.submitList(uiState.items)
                    } else {
                        binding.noTasksIcon.gone()
                        binding.noTasksText.gone()
                        binding.filteringText.let {
                            it.visible()
                            it.text = getString(uiState.filteringUiInfo.currentFilteringLabel)
                        }
                        adapter.submitList(uiState.items)
                    }
                }
            }
        }
    }

    private fun getArgs() {
        arguments?.let { args ->
            if (args.containsKey(AddTaskFragment.ARGS_KEY)) {
                val userMessage = args.getInt(AddTaskFragment.ARGS_KEY)
                binding.root.showSnackbar(userMessage, binding.fabAddTask)
                arguments?.clear()
            }
            if (args.containsKey(TaskDetailFragment.ARGS_KEY)) {
                val userMessage = args.getInt(TaskDetailFragment.ARGS_KEY)
                binding.root.showSnackbar(userMessage, binding.fabAddTask)
                arguments?.clear()
            }
            if (args.containsKey(EditTaskFragment.ARGS_KEY)) {
                val userMessage = args.getInt(EditTaskFragment.ARGS_KEY)
                binding.root.showSnackbar(userMessage, binding.fabAddTask)
                arguments?.clear()
            }
        }
    }

    companion object {
        const val ARGS_KEY = "args_key"
    }
}
