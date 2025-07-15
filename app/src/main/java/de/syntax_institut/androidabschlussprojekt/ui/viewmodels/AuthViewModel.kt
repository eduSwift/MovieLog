package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import de.syntax_institut.androidabschlussprojekt.data.database.UserEntity
import de.syntax_institut.androidabschlussprojekt.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _authReady = MutableStateFlow(false)
    val authReady: StateFlow<Boolean> = _authReady.asStateFlow()

    private val _didLogout = MutableStateFlow(false)
    val didLogout: StateFlow<Boolean> = _didLogout.asStateFlow()

    private val _justSignedUp = MutableStateFlow(false)
    val justSignedUp: StateFlow<Boolean> = _justSignedUp.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val user = firebaseAuth.currentUser
        _isAuthenticated.value = user != null
        _currentUserId.value = user?.uid
        _authReady.value = true
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                viewModelScope.launch {
                    val newUser = UserEntity(
                        uid = uid,
                        email = email,
                        nickname = "User_$uid",
                        profileImageUrl = ""
                    )
                    userRepository.insertUser(newUser)
                    _isAuthenticated.value = true
                    _currentUserId.value = uid
                    _justSignedUp.value = true
                    onSuccess()
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Registration failed")
            }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = it.user?.uid
                _isAuthenticated.value = uid != null
                _currentUserId.value = uid
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Login failed")
            }
    }


    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val userEmail = firebaseUser.email
                if (!userEmail.isNullOrEmpty()) {
                    firebaseAuth.sendPasswordResetEmail(userEmail)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onComplete(true, null)
                            } else {
                                onComplete(false, task.exception?.message)
                            }
                        }
                } else {
                    onComplete(false, "No email associated with this user.")
                }
            } else {
                onComplete(false, "User is not logged in.")
            }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _isAuthenticated.value = false
        _currentUserId.value = null
        _didLogout.value = true
    }

    fun clearLogoutFlag() {
        _didLogout.value = false
    }

    fun clearSignUpFlag() {
        _justSignedUp.value = false
    }
}