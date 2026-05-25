package com.mramfix.aifintes.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProfileApi {

    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @POST("profile/onboarding")
    suspend fun submitOnboarding(@Body request: OnboardingRequest): Response<OnboardingResponse>
}
