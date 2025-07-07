package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _wasJustRegistered = MutableStateFlow(false)
    val wasJustRegistered: StateFlow<Boolean> = _wasJustRegistered

    fun login(email: String, password: String) {
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _isAuthenticated.value = true
                        _currentUserId.value = auth.currentUser?.uid
                        _wasJustRegistered.value = false
                    } else {
                        _isAuthenticated.value = false
                        _errorMessage.value = task.exception?.localizedMessage
                    }
                }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _isAuthenticated.value = true
                        _currentUserId.value = auth.currentUser?.uid
                        _wasJustRegistered.value = true
                    } else {
                        _isAuthenticated.value = false
                        _errorMessage.value = task.exception?.localizedMessage
                    }
                }
        }
    }

    fun logout() {
        auth.signOut()
        _isAuthenticated.value = false
        _currentUserId.value = null
        _wasJustRegistered.value = false // ✅ reset
    }

    fun setError(message: String?) {
        _errorMessage.value = message
    }
}
