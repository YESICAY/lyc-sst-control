package com.lyc.sstcontrol.ui.admin

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lyc.sstcontrol.data.CreateUserError
import com.lyc.sstcontrol.data.CreateUserException
import com.lyc.sstcontrol.data.UserAdminRepository
import com.lyc.sstcontrol.data.UserRole

data class CreatedCredentials(
    val fullName: String,
    val email: String,
    val password: String
)

class CreateUserViewModel : ViewModel() {

    private val repository = UserAdminRepository()

    var fullName by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var role by mutableStateOf(UserRole.INSPECTOR)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<CreateUserError?>(null)
        private set
    var createdCredentials by mutableStateOf<CreatedCredentials?>(null)
        private set

    fun onFullNameChange(value: String) {
        fullName = value
        error = null
    }

    fun onEmailChange(value: String) {
        email = value
        error = null
    }

    fun onRoleChange(value: UserRole) {
        role = value
    }

    fun onCreateClick() {
        if (isLoading) return
        val name = fullName.trim()
        val mail = email.trim()
        if (name.isEmpty() || mail.isEmpty()) {
            error = CreateUserError.EMPTY_FIELDS
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            error = CreateUserError.INVALID_EMAIL
            return
        }

        isLoading = true
        error = null
        repository.createUser(name, mail, role) { result ->
            isLoading = false
            result
                .onSuccess { password ->
                    createdCredentials = CreatedCredentials(name, mail, password)
                }
                .onFailure {
                    error = (it as? CreateUserException)?.error ?: CreateUserError.UNKNOWN
                }
        }
    }

    fun onCredentialsAcknowledged() {
        createdCredentials = null
        fullName = ""
        email = ""
        role = UserRole.INSPECTOR
    }
}