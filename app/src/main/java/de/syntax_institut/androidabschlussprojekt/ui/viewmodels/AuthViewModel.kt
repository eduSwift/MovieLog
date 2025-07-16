package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import de.syntax_institut.androidabschlussprojekt.data.database.UserEntity
import de.syntax_institut.androidabschlussprojekt.data.repository.UserRepository
import kotlinx.coroutines.delay
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
        // This listener will update _isAuthenticated and _currentUserId when Firebase Auth state changes
        // This is important for signIn, signOut, etc. but not for the initial signUp flow.
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            _isAuthenticated.value = user != null
            _currentUserId.value = user?.uid
            _authReady.value = true
        }
    }

    // Removed checkCurrentUser() as addAuthStateListener handles initial state and changes

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

                        // IMPORTANT: Do NOT set _isAuthenticated or _currentUserId here.
                        // The user is created, but we want them to explicitly sign in.
                        _justSignedUp.value = true
                        onSuccess() // Signal to UI that account was created
                    } catch (e: Exception) {
                        onError("Failed to insert user in Room: ${e.localizedMessage}")
                        // Optionally delete Firebase user if Room insertion fails
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
                // The addAuthStateListener will handle updating _isAuthenticated and _currentUserId
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Login failed")
            }
    }

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            // This part might need to be adjusted if called when user is not logged in
            // For now, it seems fine to assume current user or prompt for email.
            // Simplified for brevity, consider specific use case for this function.
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
        // addAuthStateListener will set _isAuthenticated and _currentUserId to null
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
                                // addAuthStateListener will set _isAuthenticated and _currentUserId to null
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