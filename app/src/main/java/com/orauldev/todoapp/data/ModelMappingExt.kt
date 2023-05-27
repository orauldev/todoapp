package com.orauldev.todoapp.data

import com.orauldev.todoapp.data.source.local.LocalTask
import com.orauldev.todoapp.data.source.network.NetworkTask
import com.orauldev.todoapp.data.source.network.TaskStatus

/**
 * Data model mapping extension functions. There are three model types:
 *
 * - Task: External model exposed to other layers in the architecture.
 * Obtained using `toExternal`.
 *
 * - NetworkTask: Internal model used to represent a task from the network. Obtained using
 * `toNetwork`.
 *
 * - LocalTask: Internal model used to represent a task stored locally in a database. Obtained
 * using `toLocal`.
 *
 */

fun Task.toLocal() = LocalTask(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
)

fun NetworkTask.toLocal() = LocalTask(
    id = id,
    title = title,
    description = shortDescription,
    isCompleted = (status == TaskStatus.COMPLETE),
)

fun LocalTask.toNetwork() = NetworkTask(
    id = id,
    title = title,
    shortDescription = description,
    status = if (isCompleted) { TaskStatus.COMPLETE } else { TaskStatus.ACTIVE }
)

fun LocalTask.toExternal() = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
)

// Note: JvmName is used to provide a unique name for each extension function with the same name.
// Without this, type erasure will cause compiler errors because these methods will have the same
// signature on the JVM.
@JvmName("localToExternal")
fun List<LocalTask>.toExternal() = map(LocalTask::toExternal)

fun List<LocalTask>.toNetwork() = map(LocalTask::toNetwork)

@JvmName("networkToLocal")
fun List<NetworkTask>.toLocal() = map(NetworkTask::toLocal)

@JvmName("externalToNetwork")
fun List<Task>.toNetwork() = map(Task::toNetwork)

fun List<Task>.toLocal() = map(Task::toLocal)

@JvmName("networkToExternal")
fun List<NetworkTask>.toExternal() = map(NetworkTask::toExternal)

fun Task.toNetwork() = toLocal().toNetwork()

fun NetworkTask.toExternal() = toLocal().toExternal()
