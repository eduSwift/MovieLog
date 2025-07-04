package de.syntax_institut.androidabschlussprojekt.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE userId = :uid")
    suspend fun getMoviesForUser(uid: String): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE userId = :uid AND isFavorite = 1")
    suspend fun getFavorites(uid: String): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE userId = :uid AND isWantToWatch = 1")
    suspend fun getWantToWatch(uid: String): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE userId = :uid AND isWatched = 1")
    suspend fun getWatched(uid: String): List<MovieEntity>
}