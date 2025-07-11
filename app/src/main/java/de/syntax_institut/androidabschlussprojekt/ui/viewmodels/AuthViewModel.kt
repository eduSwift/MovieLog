package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import de.syntax_institut.androidabschlussprojekt.data.database.UserDao
import de.syntax_institut.androidabschlussprojekt.data.database.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthViewModel(
    private val userDao: UserDao
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _wasJustRegistered = MutableStateFlow(false)
    val wasJustRegistered: StateFlow<Boolean> = _wasJustRegistered

    private val _didLogout = MutableStateFlow(false)
    val didLogout: StateFlow<Boolean> = _didLogout

    private val _userEntity = MutableStateFlow<UserEntity?>(null)
    val userEntity: StateFlow<UserEntity?> = _userEntity

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val newIsAuthenticated = user != null
            val newUserId = user?.uid

            Log.d("AuthViewModel", "AuthStateListener: User: ${user?.email}, UID: $newUserId, Authenticated: $newIsAuthenticated")

            if (_isAuthenticated.value != newIsAuthenticated) {
                _isAuthenticated.value = newIsAuthenticated
                Log.d("AuthViewModel", "isAuthenticated updated to: $newIsAuthenticated")
            }
            if (_currentUserId.value != newUserId) {
                _currentUserId.value = newUserId
                Log.d("AuthViewModel", "currentUserId updated to: $newUserId")
            }

            if (!newIsAuthenticated) {
                _userEntity.value = null
                _wasJustRegistered.value = false
            } else {
                newUserId?.let { uid ->
                    if (_userEntity.value == null || _userEntity.value?.uid != uid) {
                        loadUserData(uid)
                    }
                }
            }
        }
    }


    fun loadUserData(uid: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Attempting to load user data for UID: $uid")
            try {
                val user = userDao.getUserById(uid)
                _userEntity.value = user
                Log.d("AuthViewModel", "User data loaded: $user")
                if (user == null) {
                    Log.w("AuthViewModel", "User data is null for UID: $uid. Ensure user is saved to Room during registration/first login.")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading user data for $uid: ${e.message}")
                _userEntity.value = null
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                val authResult = suspendCancellableCoroutine<AuthResult> { continuation ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                task.result?.let { continuation.resume(it) }
                                    ?: continuation.resumeWithException(IllegalStateException("Login result is null"))
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Login failed"))
                            }
                        }

                    continuation.invokeOnCancellation {

                    }
                }


                Log.d("AuthViewModel", "Login successful for $email")
                _wasJustRegistered.value = false

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed for $email: ${e.localizedMessage}")
                _errorMessage.value = e.localizedMessage ?: "Login failed."
            }
        }
    }


    fun register(email: String, password: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                val authResult = suspendCancellableCoroutine<AuthResult> { continuation ->
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                task.result?.let { continuation.resume(it) }
                                    ?: continuation.resumeWithException(IllegalStateException("Registration result is null"))
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Registration failed"))
                            }
                        }
                    continuation.invokeOnCancellation { }
                }

                val uid = authResult.user?.uid
                if (uid != null) {
                    Log.d("AuthViewModel", "Registration successful in Firebase for $email, UID: $uid")
                    _wasJustRegistered.value = true


                    try {
                        val newUser = UserEntity(
                            uid = uid,
                            email = email,
                            nickname = "New User",
                            description = "This is your profile description.",
                            profileImageUrl = "https://via.placeholder.com/150"
                        )
                        userDao.insertUser(newUser)
                        Log.d("AuthViewModel", "User data inserted into Room for $uid")
                        loadUserData(uid)
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Error inserting user into Room: ${e.message}")
                        _errorMessage.value = "Registration successful but failed to save profile data locally."
                    }
                } else {
                    _errorMessage.value = "User UID is null after Firebase registration."
                    Log.e("AuthViewModel", "User UID is null after Firebase registration for $email")
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration failed overall for $email: ${e.localizedMessage}")
                _wasJustRegistered.value = false
                _errorMessage.value = e.localizedMessage ?: "Registration failed."
            }
        }
    }

    fun logout() {
        Log.d("AuthViewModel", "Attempting logout.")
        auth.signOut()
        _wasJustRegistered.value = false
        _didLogout.value = true
        _userEntity.value = null
    }

    fun clearLogoutFlag() {
        _didLogout.value = false
        Log.d("AuthViewModel", "Logout flag cleared.")
    }

    fun clearWasJustRegisteredFlag() {
        _wasJustRegistered.value = false
        Log.d("AuthViewModel", "Was just registered flag cleared.")
    }

    fun setError(message: String?) {
        _errorMessage.value = message
        Log.e("AuthViewModel", "Error set: $message")
    }
}