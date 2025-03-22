package ru.hse.auth

data class AuthRequest(
    val email: String,
    val password: String
)