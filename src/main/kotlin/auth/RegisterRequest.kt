package ru.hse.auth

data class RegisterRequest(
    val email: String,
    val password: String
)
