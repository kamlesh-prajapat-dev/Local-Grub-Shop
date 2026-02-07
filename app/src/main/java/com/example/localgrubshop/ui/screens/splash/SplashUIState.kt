package com.example.localgrubshop.ui.screens.splash

sealed interface SplashUIState {
    object Idle: SplashUIState
    object Loading: SplashUIState
    object HomeState: SplashUIState
    object AuthState: SplashUIState
}