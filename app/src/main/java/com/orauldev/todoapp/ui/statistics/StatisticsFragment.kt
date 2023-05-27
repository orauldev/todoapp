package com.orauldev.todoapp.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.orauldev.todoapp.R
import com.orauldev.todoapp.databinding.FragmentStatisticsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticsFragment : Fragment() {
    private val binding by lazy { FragmentStatisticsBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<StatisticsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { uiState ->

                    binding.progress.isVisible = uiState.isLoading
                    binding.emptyMessageText.isVisible = uiState.isEmpty
                    binding.errorMessageText.isVisible = uiState.hasError
                    binding.layoutStatistics.isVisible = !uiState.isEmpty

                    if (!uiState.isEmpty) {
                        binding.activeTasks.text = getString(R.string.statistics_active_tasks, uiState.activeTasksPercent)
                        binding.completedTasks.text = getString(R.string.statistics_completed_tasks, uiState.completedTasksPercent)
                    }

                }
            }
        }
    }
}