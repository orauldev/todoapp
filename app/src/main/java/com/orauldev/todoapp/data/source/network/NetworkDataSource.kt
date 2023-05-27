package com.orauldev.todoapp.data.source.network

interface NetworkDataSource {

    suspend fun loadTasks(): List<NetworkTask>
}