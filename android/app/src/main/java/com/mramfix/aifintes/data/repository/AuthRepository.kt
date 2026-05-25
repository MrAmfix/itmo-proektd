package com.mramfix.aifintes.data.repository

import com.mramfix.aifintes.data.api.AuthApi
import com.mramfix.aifintes.data.api.LoginRequest
import com.mramfix.aifintes.data.api.RegisterRequest
import com.mramfix.aifintes.data.api.TokenResponse
import com.mramfix.aifintes.data.auth.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    suspend fun register(email: String, password: String): Result<TokenResponse> {
        return try {
            val response = authApi.register(RegisterRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveTokens(body.access_token, body.refresh_token)
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string().orEmpty()
                Result.failure(AuthException(errorBody, response.code()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<TokenResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveTokens(body.access_token, body.refresh_token)
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string().orEmpty()
                Result.failure(AuthException(errorBody, response.code()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class AuthException(message: String, val code: Int) : Exception(message)
