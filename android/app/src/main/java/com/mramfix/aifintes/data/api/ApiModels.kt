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

@Serializable
data class OnboardingRequest(
    val weight: Float,
    val height: Float,
    val goal: String,
    val level: String,
    val injuries: List<String>,
    val equipment: List<String>,
    val medical_disclaimer_accepted: Boolean = true
)

@Serializable
data class OnboardingResponse(
    val message: String,
    val onboarding_completed: Boolean = true
)

@Serializable
data class ProfileResponse(
    val weight: Float? = null,
    val height: Float? = null,
    val goal: String? = null,
    val level: String? = null,
    val injuries: List<String>? = null,
    val equipment: List<String>? = null,
    val onboarding_completed: Boolean = false,
    val gender: String? = null,
    val age: Int? = null
)
