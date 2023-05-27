package com.orauldev.todoapp.ui.tasks

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.orauldev.todoapp.data.Task
import com.orauldev.todoapp.databinding.TaskItemBinding

class TasksAdapter(
    private val onTaskChecked: (Task, Boolean) -> Unit,
    private val onTaskClick: (Task) -> Unit
) : ListAdapter<Task, TasksAdapter.ViewHolder>(TaskDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.onCheckedChangeListener = { isChecked -> onTaskChecked(item, isChecked) }
        holder.itemView.setOnClickListener { onTaskClick(item) }
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var onCheckedChangeListener: ((isChecked: Boolean) -> Unit)? = null
        fun bind(item: Task) {

            binding.completeCheckbox.let { checkBox ->
                if (item.isCompleted) {
                    checkBox.isChecked = true
                    checkBox.setOnClickListener { onCheckedChangeListener?.invoke(false) }
                } else {
                    checkBox.isChecked = false
                    checkBox.setOnClickListener { onCheckedChangeListener?.invoke(true) }
                }
            }

            binding.titleText.let { titleText ->
                titleText.text = item.title
                if (item.isCompleted) {
                    titleText.paintFlags = titleText.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    titleText.paintFlags =
                        titleText.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}