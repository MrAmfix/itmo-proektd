package com.mramfix.aifintes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mramfix.aifintes.data.auth.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    tokenManager: TokenManager
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean?> = tokenManager.hasTokens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
