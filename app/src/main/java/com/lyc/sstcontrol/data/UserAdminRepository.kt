package com.lyc.sstcontrol.data

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.security.SecureRandom

enum class CreateUserError {
    EMPTY_FIELDS,
    INVALID_EMAIL,
    EMAIL_IN_USE,
    NO_NETWORK,
    NOT_ALLOWED,
    UNKNOWN
}

class CreateUserException(val error: CreateUserError) : Exception()

class UserAdminRepository {

    private val db = FirebaseFirestore.getInstance()

    fun createUser(
        fullName: String,
        email: String,
        role: UserRole,
        onResult: (Result<String>) -> Unit
    ) {
        val password = generatePassword()
        val secondary = secondaryAuth()

        secondary.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val newUser = authResult.user
                if (newUser == null) {
                    secondary.signOut()
                    onResult(Result.failure(CreateUserException(CreateUserError.UNKNOWN)))
                    return@addOnSuccessListener
                }

                val profile = mapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "role" to if (role == UserRole.ADMIN) "admin" else "inspector",
                    "active" to true
                )

                db.collection("users").document(newUser.uid).set(profile)
                    .addOnSuccessListener {
                        secondary.signOut()
                        onResult(Result.success(password))
                    }
                    .addOnFailureListener { error ->
                        newUser.delete().addOnCompleteListener {
                            secondary.signOut()
                            onResult(Result.failure(error.toCreateUserException()))
                        }
                    }
            }
            .addOnFailureListener { onResult(Result.failure(it.toCreateUserException())) }
    }

    private fun secondaryAuth(): FirebaseAuth {
        val defaultApp = FirebaseApp.getInstance()
        val app = try {
            FirebaseApp.getInstance(SECONDARY_APP_NAME)
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(defaultApp.applicationContext, defaultApp.options, SECONDARY_APP_NAME)
        }
        return FirebaseAuth.getInstance(app)
    }

    private fun generatePassword(length: Int = 10): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
        val random = SecureRandom()
        return (1..length).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    private fun Throwable.toCreateUserException(): CreateUserException = when (this) {
        is CreateUserException -> this
        is FirebaseAuthUserCollisionException -> CreateUserException(CreateUserError.EMAIL_IN_USE)
        is FirebaseAuthInvalidCredentialsException -> CreateUserException(CreateUserError.INVALID_EMAIL)
        is FirebaseNetworkException -> CreateUserException(CreateUserError.NO_NETWORK)
        is FirebaseFirestoreException -> when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                CreateUserException(CreateUserError.NOT_ALLOWED)
            FirebaseFirestoreException.Code.UNAVAILABLE ->
                CreateUserException(CreateUserError.NO_NETWORK)
            else -> CreateUserException(CreateUserError.UNKNOWN)
        }
        else -> CreateUserException(CreateUserError.UNKNOWN)
    }

    private companion object {
        const val SECONDARY_APP_NAME = "secondary"
    }
}