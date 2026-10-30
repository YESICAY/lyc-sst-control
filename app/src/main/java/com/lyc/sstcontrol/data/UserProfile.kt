package com.lyc.sstcontrol.data

enum class UserRole { ADMIN, INSPECTOR }

data class UserProfile(
    val uid: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val active: Boolean
)