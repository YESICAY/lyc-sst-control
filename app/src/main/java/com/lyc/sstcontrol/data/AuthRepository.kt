package com.lyc.sstcontrol.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun hasSession(): Boolean = auth.currentUser != null

    fun signIn(email: String, password: String, onResult: (Result<UserProfile>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid == null) {
                    onResult(Result.failure(LoginException(LoginError.UNKNOWN)))
                } else {
                    loadProfile(uid, onResult)
                }
            }
            .addOnFailureListener { onResult(Result.failure(it.toLoginException())) }
    }

    fun restoreSession(onResult: (Result<UserProfile>) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(Result.failure(LoginException(LoginError.UNKNOWN)))
        } else {
            loadProfile(uid, onResult)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun sendPasswordReset(email: String, onResult: (Result<Unit>) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener {
                val error = when (it) {
                    is FirebaseAuthInvalidCredentialsException,
                    is FirebaseAuthInvalidUserException -> LoginException(LoginError.INVALID_EMAIL)
                    else -> it.toLoginException()
                }
                onResult(Result.failure(error))
            }
    }

    private fun loadProfile(uid: String, onResult: (Result<UserProfile>) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val role = when (doc.getString("role")) {
                    "admin" -> UserRole.ADMIN
                    "inspector" -> UserRole.INSPECTOR
                    else -> null
                }
                if (!doc.exists() || role == null) {
                    denyAccess(LoginError.NO_PROFILE, onResult)
                    return@addOnSuccessListener
                }
                if (doc.getBoolean("active") != true) {
                    denyAccess(LoginError.INACTIVE, onResult)
                    return@addOnSuccessListener
                }
                onResult(
                    Result.success(
                        UserProfile(
                            uid = uid,
                            fullName = doc.getString("fullName").orEmpty(),
                            email = doc.getString("email").orEmpty(),
                            role = role,
                            active = true
                        )
                    )
                )
            }
            .addOnFailureListener { onResult(Result.failure(it.toLoginException())) }
    }

    private fun denyAccess(error: LoginError, onResult: (Result<UserProfile>) -> Unit) {
        auth.signOut()
        onResult(Result.failure(LoginException(error)))
    }

    private fun Throwable.toLoginException(): LoginException = when (this) {
        is LoginException -> this
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> LoginException(LoginError.INVALID_CREDENTIALS)
        is FirebaseNetworkException -> LoginException(LoginError.NO_NETWORK)
        is FirebaseTooManyRequestsException -> LoginException(LoginError.TOO_MANY_ATTEMPTS)
        is FirebaseFirestoreException ->
            if (code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                LoginException(LoginError.NO_NETWORK)
            } else {
                LoginException(LoginError.UNKNOWN)
            }
        else -> LoginException(LoginError.UNKNOWN)
    }
}