package ru.hse.task

data class TaskRequest(
    val title: String,
    val description: String? = null
)