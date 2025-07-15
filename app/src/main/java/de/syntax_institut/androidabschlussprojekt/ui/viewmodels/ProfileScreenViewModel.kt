package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import de.syntax_institut.androidabschlussprojekt.data.database.UserDao
import de.syntax_institut.androidabschlussprojekt.data.database.UserEntity
import de.syntax_institut.androidabschlussprojekt.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileScreenViewModel(
    private val userDao: UserDao
) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<UserEntity>>(UiState.Loading)
    val userState: StateFlow<UiState<UserEntity>> = _userState.asStateFlow()

    fun loadUserData(uid: String) {
        viewModelScope.launch {
            _userState.value = UiState.Loading
            try {
                val user = userDao.getUserById(uid)
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

    fun updateNickname(uid: String, newNickname: String) {
        viewModelScope.launch {
            try {
                userDao.updateNickname(uid, newNickname)
                loadUserData(uid)
            } catch (e: Exception) {
                _userState.value = UiState.Error("Failed to update nickname: ${e.localizedMessage}")
            }
        }
    }

    fun uploadProfileImage(uid: String, imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("profileImages/$uid/${UUID.randomUUID()}.jpg")

        viewModelScope.launch {
            try {
                storageRef.putFile(imageUri).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        viewModelScope.launch {
                            userDao.updateProfileImageUrl(uid, downloadUrl.toString())
                            loadUserData(uid)
                        }
                    }
                }.addOnFailureListener {
                    _userState.value = UiState.Error("Image upload failed: ${it.localizedMessage}")
                }
            } catch (e: Exception) {
                _userState.value = UiState.Error("Image upload failed: ${e.localizedMessage}")
            }
        }
    }

    fun clearUserData() {
        _userState.value = UiState.Loading
    }
}