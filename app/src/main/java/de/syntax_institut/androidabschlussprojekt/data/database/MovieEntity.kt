package de.syntax_institut.androidabschlussprojekt.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val userId: String,
    val title: String,
    val posterPath: String,
    val overview: String,
    val releaseDate: String,
    val isWantToWatch: Boolean = false,
    val isWatched: Boolean = false,
    val isFavorite: Boolean = false
)
