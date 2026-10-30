package com.lyc.sstcontrol.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.lyc.sstcontrol.data.AuthRepository
import com.lyc.sstcontrol.data.LoginError
import com.lyc.sstcontrol.data.LoginException
import com.lyc.sstcontrol.data.UserProfile

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<LoginError?>(null)
        private set
    var profile by mutableStateOf<UserProfile?>(null)
        private set

    var resetSent by mutableStateOf(false)
        private set
    var checkingSession by mutableStateOf(repository.hasSession())
        private set

    init {
        if (checkingSession) {
            repository.restoreSession { result ->
                profile = result.getOrNull()
                checkingSession = false
            }
        }
    }

    fun onEmailChange(value: String) {
        email = value
        error = null
        resetSent = false
    }

    fun onPasswordChange(value: String) {
        password = value
        error = null
        resetSent = false
    }

    fun onSignInClick() {
        if (isLoading) return
        if (email.isBlank() || password.isBlank()) {
            error = LoginError.EMPTY_FIELDS
            return
        }
        isLoading = true
        error = null
        repository.signIn(email.trim(), password) { result ->
            isLoading = false
            result
                .onSuccess { profile = it }
                .onFailure { error = (it as? LoginException)?.error ?: LoginError.UNKNOWN }
        }
    }

    fun onForgotPasswordClick() {
        if (isLoading) return
        val mail = email.trim()
        if (mail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            error = LoginError.INVALID_EMAIL
            return
        }
        isLoading = true
        error = null
        resetSent = false
        repository.sendPasswordReset(mail) { result ->
            isLoading = false
            result
                .onSuccess { resetSent = true }
                .onFailure { error = (it as? LoginException)?.error ?: LoginError.UNKNOWN }
        }
    }

    fun signOut() {
        repository.signOut()
        profile = null
        password = ""
    }
}