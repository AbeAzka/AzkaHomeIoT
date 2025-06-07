package com.indodevstudio.azka_home_iot

data class AccountData(
    val email: String,
    val token: String,
    val provider: AuthProvider,
    val avatarUrl: String?,
    val username: String?,
    val isVerified: Boolean
)

enum class AuthProvider {
    FIREBASE, CUSTOM
}

