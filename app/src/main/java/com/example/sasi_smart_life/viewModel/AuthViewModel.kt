package com.example.sasi_smart_life.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()
    private val db = FirebaseDatabase.getInstance("https://sasi-smart-life-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

    private val tag = "AuthViewModel_SASI"


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState(isLoading = false, info = "Login successful.")
                    } else {
                        _authState.value = AuthState(isLoading = false, error = task.exception?.message)
                    }
                }
        }
    }

    fun daftar(name: String, email: String, password: String) {
        _authState.value = AuthState(isLoading = true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val uid = auth.currentUser!!.uid

                    val userData = mapOf(
                        "name" to name,
                        "email" to email
                    )

                    db.child("users").child(uid)
                        .setValue(userData)
                        .addOnSuccessListener {
                            _authState.value = AuthState(
                                isLoading = false,
                                info = "Registrasi berhasil!"
                            )
                            Log.d(tag, "User saved successfully!")

                        }
                        .addOnFailureListener {
                            _authState.value = AuthState(
                                isLoading = false,
                                error = "Tidak bisa menyimpan ke database: ${it.message}"
                            )
                            Log.e(tag, "Firebase DB Error: ${it.message}")
                        }

                } else {
                    _authState.value = AuthState(
                        isLoading = false,
                        error = task.exception?.message
                    )
                }
            }
    }


    fun kirimKodeVerifikasi(email: String) {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {
            if (it.isSuccessful) {
                _authState.value = _authState.value.copy(info = "Verification email sent.")
            } else {
                _authState.value = _authState.value.copy(error = "Failed to send verification email. Please try again later.")
            }
        }
    }

    fun clearMessages() {
        _authState.value = _authState.value.copy(error = null, info = null)
    }
}
