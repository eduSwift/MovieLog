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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthViewModel(
    private val userDao: UserDao
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _authReady = MutableStateFlow(false)
    val authReady: StateFlow<Boolean> = _authReady.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentUserId = MutableStateFlow(auth.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _wasJustRegistered = MutableStateFlow(false)
    val wasJustRegistered: StateFlow<Boolean> = _wasJustRegistered.asStateFlow()

    private val _didLogout = MutableStateFlow(false)
    val didLogout: StateFlow<Boolean> = _didLogout.asStateFlow()

    private val _showRegistrationSuccess = MutableStateFlow(false)
    val showRegistrationSuccess: StateFlow<Boolean> = _showRegistrationSuccess.asStateFlow()

    private val _userEntity = MutableStateFlow<UserEntity?>(null)
    val userEntity: StateFlow<UserEntity?> = _userEntity.asStateFlow()

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
                // If not authenticated, clear user specific data
                _userEntity.value = null
                // IMPORTANT: DO NOT CLEAR _wasJustRegistered.value here. It's cleared by AuthScreen
                Log.d("AuthViewModel", "AuthStateListener: User is not authenticated. Clearing user data.")
            } else {
                // If authenticated, ensure user data is loaded
                newUserId?.let { uid ->
                    if (_userEntity.value == null || _userEntity.value?.uid != uid) {
                        Log.d("AuthViewModel", "AuthStateListener: Attempting to load user data for UID: $uid")
                        loadUserData(uid)
                    } else {
                        Log.d("AuthViewModel", "AuthStateListener: User data already loaded for UID: $uid (Nickname: ${_userEntity.value?.nickname ?: "N/A"})")
                    }
                }
            }
            _authReady.value = true
            Log.d("AuthViewModel", "Auth check complete, _authReady set to true.")
        }
        // This initial check for authReady is mainly for cases where AuthStateListener might be slightly delayed
        // or for quick ViewModel re-creation where the listener might not be immediately re-added.
        // The listener itself is the definitive source.
        if (auth.currentUser != null && !_authReady.value) {
            _authReady.value = true
            Log.d("AuthViewModel", "AuthViewModel initialized with existing user, setting authReady to true.")
        }
    }

    fun loadUserData(uid: String) {
        viewModelScope.launch {
            userDao.observeUserById(uid).collect { user ->
                if (user != null) {
                    _userEntity.value = user
                    Log.d("AuthViewModel", "User data observed and set from Room: ${user.email}")
                } else {
                    Log.w("AuthViewModel", "User not found. Creating new.")
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null && firebaseUser.uid == uid && firebaseUser.email != null) {
                        val newUser = UserEntity(
                            uid = uid,
                            email = firebaseUser.email!!,
                            nickname = "User_${uid.take(6)}",
                            profileImageUrl = "https://via.placeholder.com/150"
                        )
                        userDao.insertUser(newUser)
                        Log.d("AuthViewModel", "Inserted new user. Room will now emit update.")
                    }
                }
            }
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            _wasJustRegistered.value = false // Clear this on any login attempt
            Log.d("AuthViewModel", "Attempting Firebase login for $email")

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
                    continuation.invokeOnCancellation { Log.d("AuthViewModel", "Firebase login coroutine was cancelled for $email.") }
                }
                Log.d("AuthViewModel", "Login successful for ${email}. Firebase UID: ${authResult.user?.uid}")
                // AuthStateListener will now correctly set _isAuthenticated to true and load user data.
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed for $email: ${e.localizedMessage}")
                _errorMessage.value = e.localizedMessage ?: "Login failed."
                _isAuthenticated.value = false
                _currentUserId.value = null
                _userEntity.value = null
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            _didLogout.value = false
            _wasJustRegistered.value = true
            _showRegistrationSuccess.value = true // ✅ trigger display
            Log.d("AuthViewModel", "Attempting Firebase registration for $email")

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
                    continuation.invokeOnCancellation {
                        Log.d("AuthViewModel", "Firebase registration coroutine was cancelled for $email.")
                    }
                }

                val firebaseUser = authResult.user
                val uid = firebaseUser?.uid

                if (uid != null && firebaseUser.email != null) {
                    Log.d("AuthViewModel", "Firebase registration successful for $email. UID: $uid")

                    val newUserEntity = UserEntity(
                        uid = uid,
                        email = firebaseUser.email!!,
                        nickname = "User_${uid.substring(0, 6)}",
                        profileImageUrl = "https://via.placeholder.com/150"
                    )

                    try {
                        userDao.insertUser(newUserEntity)
                        Log.d("AuthViewModel", "User data successfully inserted into Room for UID: $uid (after registration)")

                        _wasJustRegistered.value = true // Signal UI about successful registration
                        _userEntity.value = null // Clear user data as user is not logged in yet
                        _currentUserId.value = null // Clear ID as user is not logged in yet

                        // Sign out the user immediately after registration
                        auth.signOut()
                        Log.d("AuthViewModel", "Successfully signed out newly registered user from Firebase.")

                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "Error inserting user into Room after Firebase registration for UID $uid: ${e.message}")
                        _errorMessage.value = "Registration successful in Firebase, but failed to save profile locally: ${e.localizedMessage}"
                        firebaseUser.delete().addOnCompleteListener { Log.d("AuthViewModel", "Deleted Firebase user due to Room storage failure.") }
                        _wasJustRegistered.value = false
                    }
                } else {
                    _errorMessage.value = "Firebase registration succeeded but user UID or email was null."
                    Log.e("AuthViewModel", "Firebase registration succeeded but user UID or email was null for $email")
                    _wasJustRegistered.value = false
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Firebase registration failed for $email: ${e.localizedMessage}")
                _wasJustRegistered.value = false
                _errorMessage.value = e.localizedMessage ?: "Registration failed. Please try again."
            }
        }
    }

    fun setShowRegistrationSuccess(value: Boolean) {
        _showRegistrationSuccess.value = value
    }

    fun clearRegistrationSuccessMessage() {
        _showRegistrationSuccess.value = false
    }

    fun clearUserData() {
        _userEntity.value = null
        _currentUserId.value = null
        _isAuthenticated.value = false
        _errorMessage.value = null
        _wasJustRegistered.value = false // This is okay here, for a full data clear
        _didLogout.value = false
        Log.d("AuthViewModel", "Cleared user authentication and profile data.")
    }

    fun updateNickname(uid: String, newNickname: String) {
        viewModelScope.launch {
            try {
                userDao.updateNickname(uid, newNickname)
                loadUserData(uid) // refresh profile
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to update nickname: ${e.localizedMessage}")
            }
        }
    }


    fun logout() {
        Log.d("AuthViewModel", "Attempting logout.")
        auth.signOut()
        _didLogout.value = true
        Log.d("AuthViewModel", "Firebase signOut called.")
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
        if (message != null) {
            Log.e("AuthViewModel", "Error set: $message")
        } else {
            Log.d("AuthViewModel", "Error message cleared.")
        }
    }
}