package de.syntax_institut.androidabschlussprojekt.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete // <-- Ensure this import is present
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE userId = :userId AND isFavorite = 1")
    fun getFavoriteMovies(userId: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE userId = :userId AND isWatched = 1")
    fun getWatchedMovies(userId: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE userId = :userId AND isWantToWatch = 1")
    fun getWantToWatchMovies(userId: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE tmdbMovieId = :tmdbMovieId AND userId = :userId LIMIT 1")
    suspend fun getMovieByIdAndUserId(tmdbMovieId: Int, userId: String): MovieEntity?

    @Delete
    suspend fun deleteMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE userId = :userId")
    fun getMoviesForUser(userId: String): Flow<List<MovieEntity>>

    @Query("DELETE FROM movies WHERE userId = :userId")
    suspend fun deleteAllMoviesByUserId(userId: String)
}