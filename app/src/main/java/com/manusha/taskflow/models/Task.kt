package com.manusha.taskflow.models

data class Task(
    var id: String = "",
    val title: String = "",
    val completed: Boolean = false,
    val userId: String = "",
    val timestamp: Long = 0
)
