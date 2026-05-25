package com.mramfix.aifintes.data.api

import kotlinx.serialization.Serializable

// Базовые DTO для ответов API

@Serializable
data class ApiError(
    val detail: String
)

@Serializable
data class TokenResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String = "bearer"
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)
