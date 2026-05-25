package com.mramfix.aifintes.data.auth

import com.mramfix.aifintes.data.api.AuthApi
import com.mramfix.aifintes.data.api.TokenResponse
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authApi: Lazy<AuthApi>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Only retry once
        if (responseCount(response) >= 2) {
            return null
        }

        val refreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null

        return runBlocking {
            try {
                val refreshResponse = authApi.get().login(
                    com.mramfix.aifintes.data.api.LoginRequest(
                        email = "",  // Бэкенд должен уметь обновлять по refresh_token
                        password = refreshToken
                    )
                )
                if (refreshResponse.isSuccessful) {
                    val body = refreshResponse.body()
                    if (body != null) {
                        tokenManager.saveTokens(body.access_token, body.refresh_token)
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${body.access_token}")
                            .build()
                    } else null
                } else {
                    tokenManager.clearTokens()
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
