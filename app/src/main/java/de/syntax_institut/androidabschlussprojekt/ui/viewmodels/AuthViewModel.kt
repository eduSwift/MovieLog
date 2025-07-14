package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import de.syntax_institut.androidabschlussprojekt.data.database.UserDao
import de.syntax_institut.androidabschlussprojekt.data.database.UserEntity
import de.syntax_institut.androidabschlussprojekt.ui.state.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthViewModel(
    private val userDao: UserDao
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _userState: MutableStateFlow<UiState<UserEntity>> =
        MutableStateFlow(UiState.Loading)
    val userState: StateFlow<UiState<UserEntity>> = _userState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _authReady = MutableStateFlow(false)
    val authReady: StateFlow<Boolean> = _authReady.asStateFlow()

    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _didLogout = MutableStateFlow(false)
    val didLogout: StateFlow<Boolean> = _didLogout.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _wasJustRegistered = MutableStateFlow(false)
    val wasJustRegistered: StateFlow<Boolean> = _wasJustRegistered.asStateFlow()

    private val _showRegistrationSuccess = MutableStateFlow(false)
    val showRegistrationSuccess: StateFlow<Boolean> = _showRegistrationSuccess.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isAuthenticated.value = user != null
            _currentUserId.value = user?.uid

            if (user != null) {
                loadUserData(user.uid)
            } else {
                _userState.value = UiState.Loading
            }

            _authReady.value = true
        }
    }

    fun loadUserData(uid: String) {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            try {
                val user = userDao.getUserById(uid)

                if (user != null) {
                    _userState.value = UiState.Success(user)
                } else {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null && firebaseUser.uid == uid && firebaseUser.email != null) {
                        val newUser = UserEntity(
                            uid = uid,
                            email = firebaseUser.email!!,
                            nickname = "User_${uid.take(6)}",
                            profileImageUrl = "https://via.placeholder.com/150"
                        )
                        userDao.insertUser(newUser)
                        _userState.value = UiState.Success(newUser)
                    } else {
                        _userState.value = UiState.Error("No Firebase user data available.")
                    }
                }
            } catch (e: Exception) {
                _userState.value = UiState.Error("Error loading user: ${e.localizedMessage}")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                suspendCancellableCoroutine<AuthResult> { continuation ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                task.result?.let { continuation.resume(it) }
                                    ?: continuation.resumeWithException(Exception("Login failed"))
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Login failed"))
                            }
                        }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Login failed"
                _userState.value = UiState.Error("Login error: ${e.localizedMessage}")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            _wasJustRegistered.value = true
            _showRegistrationSuccess.value = true

            try {
                val authResult = suspendCancellableCoroutine<AuthResult> { continuation ->
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                task.result?.let { continuation.resume(it) }
                                    ?: continuation.resumeWithException(Exception("Registration failed"))
                            } else {
                                continuation.resumeWithException(task.exception ?: Exception("Registration failed"))
                            }
                        }
                }

                val firebaseUser = authResult.user
                if (firebaseUser != null && firebaseUser.email != null) {
                    val newUser = UserEntity(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email!!,
                        nickname = "User_${firebaseUser.uid.take(6)}",
                        profileImageUrl = "https://via.placeholder.com/150"
                    )
                    userDao.insertUser(newUser)
                    auth.signOut()
                }

            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Registration failed"
                _userState.value = UiState.Error("Registration error: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _didLogout.value = true
        _userState.value = UiState.Loading
        _currentUserId.value = null
    }

    fun clearLogoutFlag() {
        _didLogout.value = false
    }

    fun clearUserData() {
        _userState.value = UiState.Loading
        _currentUserId.value = null
        _isAuthenticated.value = false
        _errorMessage.value = null
    }

    fun updateNickname(uid: String, newNickname: String) {
        viewModelScope.launch {
            try {
                userDao.updateNickname(uid, newNickname)
                loadUserData(uid)
            } catch (e: Exception) {
                _userState.value = UiState.Error("Nickname update failed: ${e.localizedMessage}")
            }
        }
    }

    fun setShowRegistrationSuccess(value: Boolean) {
        _showRegistrationSuccess.value = value
    }

    fun clearRegistrationSuccessMessage() {
        _showRegistrationSuccess.value = false
    }

    fun clearWasJustRegisteredFlag() {
        _wasJustRegistered.value = false
    }

    fun setError(message: String?) {
        _errorMessage.value = message
    }
}