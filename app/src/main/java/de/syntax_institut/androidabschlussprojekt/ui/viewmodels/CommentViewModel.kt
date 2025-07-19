package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.androidabschlussprojekt.data.database.CommentEntity
import de.syntax_institut.androidabschlussprojekt.data.database.UserEntity
import de.syntax_institut.androidabschlussprojekt.data.repository.CommentRepository
import de.syntax_institut.androidabschlussprojekt.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CommentViewModel(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _movieId = MutableStateFlow<Int?>(null)

    private val _comments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val comments: StateFlow<List<CommentEntity>> = _comments

    private val _commentCount = MutableStateFlow(0)
    val commentCount: StateFlow<Int> = _commentCount

    suspend fun getUserById(uid: String): UserEntity? {
        return userRepository.getUser(uid)
    }


    val commentsFlow: StateFlow<List<CommentEntity>> = _movieId
        .filterNotNull()
        .flatMapLatest { movieId ->
            commentRepository.getCommentsForMovie(movieId)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun loadComments(movieId: Int) {
        viewModelScope.launch {
            commentRepository.getCommentsForMovie(movieId).collect { comments ->
                _comments.value = comments
                _commentCount.value = comments.size
            }
        }
    }

    fun addComment(userId: String, movieId: Int, text: String) {
        viewModelScope.launch {
            val comment = CommentEntity(
                userId = userId,
                movieId = movieId,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            commentRepository.addComment(comment)
        }
    }

    fun deleteComment(comment: CommentEntity) {
        viewModelScope.launch {
            commentRepository.deleteComment(comment)
        }
    }

    fun updateComment(comment: CommentEntity) {
        viewModelScope.launch {
            commentRepository.updateComment(comment)
        }
    }
}