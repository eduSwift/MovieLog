package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.androidabschlussprojekt.data.database.MovieDao
import de.syntax_institut.androidabschlussprojekt.data.database.MovieEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieViewModel(private val movieDao: MovieDao) : ViewModel() {

    private val _favorites = MutableStateFlow<List<MovieEntity>>(emptyList())
    val favorites: StateFlow<List<MovieEntity>> = _favorites

    private val _watched = MutableStateFlow<List<MovieEntity>>(emptyList())
    val watched: StateFlow<List<MovieEntity>> = _watched

    private val _wantToWatch = MutableStateFlow<List<MovieEntity>>(emptyList())
    val wantToWatch: StateFlow<List<MovieEntity>> = _wantToWatch

    fun toggleFlag(
        userId: String,
        movie: MovieEntity,
        type: String
    ) = viewModelScope.launch {
        val updated = when (type) {
            "favorite" -> movie.copy(isFavorite = !movie.isFavorite)
            "watched" -> movie.copy(isWatched = !movie.isWatched)
            "wantToWatch" -> movie.copy(isWantToWatch = !movie.isWantToWatch)
            else -> movie
        }
        movieDao.insertMovie(updated)
        refreshUserMovies(userId)
    }

    fun refreshUserMovies(userId: String) = viewModelScope.launch {
        _favorites.value = movieDao.getFavorites(userId)
        _watched.value = movieDao.getWatched(userId)
        _wantToWatch.value = movieDao.getWantToWatch(userId)
    }

    fun removeMovie(userId: String, movie: MovieEntity, type: String) = viewModelScope.launch {
        val updated = when (type) {
            "favorite" -> movie.copy(isFavorite = false)
            "watched" -> movie.copy(isWatched = false)
            "wantToWatch" -> movie.copy(isWantToWatch = false)
            else -> movie
        }
        movieDao.insertMovie(updated)
        refreshUserMovies(userId)
    }
}