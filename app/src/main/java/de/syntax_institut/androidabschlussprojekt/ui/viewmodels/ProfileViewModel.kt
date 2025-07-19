package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import de.syntax_institut.androidabschlussprojekt.data.database.UserEntity
import de.syntax_institut.androidabschlussprojekt.data.repository.UserRepository
import de.syntax_institut.androidabschlussprojekt.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<UserEntity>>(UiState.Loading)
    val userState: StateFlow<UiState<UserEntity>> = _userState.asStateFlow()

    fun loadUserData(uid: String) {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            try {
                val user = userRepository.getUser(uid)
                if (user != null) {
                    _userState.value = UiState.Success(user)
                } else {
                    _userState.value = UiState.Error("User not found in database.")
                }
            } catch (e: Exception) {
                _userState.value = UiState.Error("Error loading profile: ${e.localizedMessage}")
            }
        }
    }

    fun markProfileComplete(uid: String) {
        viewModelScope.launch {
            userRepository.markProfileComplete(uid)
        }
    }

    fun updateNickname(uid: String, newNickname: String) {
        viewModelScope.launch {
            try {
                userRepository.updateNickname(uid, newNickname)
                loadUserData(uid)
            } catch (e: Exception) {
                _userState.value = UiState.Error("Failed to update nickname: ${e.localizedMessage}")
            }
        }
    }

    fun uploadProfileImage(uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                }
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url") as? String
                    url?.let { onSuccess(it) }
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError(error?.description ?: "Unknown error")
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    fun setProfileImageManually(uid: String, imageUrl: String) {
        viewModelScope.launch {
            userRepository.uploadProfileImage(uid, imageUrl)
            loadUserData(uid)
        }
    }

    fun clearUserData() {
        _userState.value = UiState.Loading
    }
}