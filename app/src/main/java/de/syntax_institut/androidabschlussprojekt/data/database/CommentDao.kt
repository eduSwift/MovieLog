package de.syntax_institut.androidabschlussprojekt.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Query("SELECT * FROM comments WHERE movieId = :movieId ORDER BY timestamp DESC")
    fun getCommentsForMovie(movieId: Int): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertComment(comment: CommentEntity)

    @Update
    suspend fun updateComment(comment: CommentEntity)

    @Delete
    suspend fun deleteComment(comment: CommentEntity)
}