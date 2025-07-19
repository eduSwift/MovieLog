package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
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

    private val _didDeleteAccount = MutableStateFlow(false)
    val didDeleteAccount: StateFlow<Boolean> = _didDeleteAccount.asStateFlow()

    private val _justSignedUp = MutableStateFlow(false)
    val justSignedUp: StateFlow<Boolean> = _justSignedUp.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            val isUserAuthenticated = user != null
            val userId = user?.uid

            _isAuthenticated.value = isUserAuthenticated
            _currentUserId.value = userId

            if (!_authReady.value) {
                _authReady.value = true
            }
        }
    }


    fun signUp(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                viewModelScope.launch {
                    try {
                        val newUser = UserEntity(
                            uid = uid,
                            email = email,
                            nickname = "",
                            profileImageUrl = "",
                            isProfileComplete = false
                        )
                        userRepository.insertUser(newUser)
                        firebaseAuth.signOut()
                        _justSignedUp.value = true
                        onSuccess()
                    } catch (e: Exception) {
                        onError("Failed to insert user in Room: ${e.localizedMessage}")
                        result.user?.delete()
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Registration failed")
            }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Login failed")
            }
    }

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(true, null)
                    } else {
                        onComplete(false, task.exception?.message)
                    }
                }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _didLogout.value = true
    }

    fun clearLogoutFlag() {
        _didLogout.value = false
    }

    fun clearSignUpFlag() {
        _justSignedUp.value = false
    }

    fun deleteAccount(
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = firebaseAuth.currentUser
        val uid = user?.uid
        val email = user?.email

        if (user == null || uid == null || email.isNullOrBlank()) {
            onError("User not logged in or missing credentials.")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                viewModelScope.launch {
                    try {
                        userRepository.deleteUser(uid)
                        user.delete()
                            .addOnSuccessListener {
                                _didDeleteAccount.value = true
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                onError("Failed to delete Firebase user: ${e.message}")
                            }
                    } catch (e: Exception) {
                        onError("Failed to delete user data: ${e.localizedMessage}")
                    }
                }
            }
            .addOnFailureListener { e ->
                onError("Re-authentication failed: ${e.message}")
            }
    }

    fun clearDeleteFlag() {
        _didDeleteAccount.value = false
    }
}