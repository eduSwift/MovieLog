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

    // These methods return Flow<List<MovieEntity>> as discussed for live updates
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

    // --- ADDED THIS METHOD TO FIX 'Unresolved reference: getMoviesForUser' ---
    // Returns all movies for a specific user, suitable for observation.
    @Query("SELECT * FROM movies WHERE userId = :userId")
    fun getMoviesForUser(userId: String): Flow<List<MovieEntity>>

    // --- ADDED THIS METHOD TO FIX 'Unresolved reference: deleteAllMoviesByUserId' ---
    // Deletes all movie entities associated with a given user ID.
    @Query("DELETE FROM movies WHERE userId = :userId")
    suspend fun deleteAllMoviesByUserId(userId: String)
}