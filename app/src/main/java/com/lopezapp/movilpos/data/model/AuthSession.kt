package com.lopezapp.movilpos.data.model

data class AuthSession(
    val user: User,
    val loggedInAtMillis: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = true
)
