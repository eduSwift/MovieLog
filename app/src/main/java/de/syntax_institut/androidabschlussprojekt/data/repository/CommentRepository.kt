package de.syntax_institut.androidabschlussprojekt.data.repository


import de.syntax_institut.androidabschlussprojekt.data.database.CommentDao
import de.syntax_institut.androidabschlussprojekt.data.database.CommentEntity
import kotlinx.coroutines.flow.Flow

class CommentRepository(
    private val commentDao: CommentDao
) {
    fun getCommentsForMovie(movieId: Int): Flow<List<CommentEntity>> {
        return commentDao.getCommentsForMovie(movieId)
    }

    suspend fun addComment(comment: CommentEntity) {
        commentDao.insertComment(comment)
    }

    suspend fun updateComment(comment: CommentEntity) {
        commentDao.updateComment(comment)
    }

    suspend fun deleteComment(comment: CommentEntity) {
        commentDao.deleteComment(comment)
    }
}