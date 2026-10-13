package com.lopezapp.movilpos.data.model

data class FirebaseSyncConfig(
    val isAutoSyncEnabled: Boolean = true,
    val intervalMinutes: Int = 15, // e.g. 5, 15, 30, 60 minutes
    val lastSyncTimestampMillis: Long? = null
)
