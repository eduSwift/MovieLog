package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.androidabschlussprojekt.data.database.MovieEntity
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory
import de.syntax_institut.androidabschlussprojekt.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _searchResult = MutableStateFlow<List<Movie>>(emptyList())
    val searchResult: StateFlow<List<Movie>> = _searchResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _favorites = MutableStateFlow<List<MovieEntity>>(emptyList())
    val favorites: StateFlow<List<MovieEntity>> = _favorites.asStateFlow()

    private val _watched = MutableStateFlow<List<MovieEntity>>(emptyList())
    val watched: StateFlow<List<MovieEntity>> = _watched.asStateFlow()

    private val _wantToWatch = MutableStateFlow<List<MovieEntity>>(emptyList())
    val wantToWatch: StateFlow<List<MovieEntity>> = _wantToWatch.asStateFlow()

    private val _isLoadingMovies = MutableStateFlow(false)
    val isLoadingMovies: StateFlow<Boolean> = _isLoadingMovies.asStateFlow()


    fun getMovies(category: MovieCategory) {
        viewModelScope.launch {
            try {
                _isLoadingMovies.value = true
                val fetchedMovies = repository.getMoviesByCategory(category)
                _movies.value = fetchedMovies
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error fetching movies: ${e.message}"
            } finally {
                _isLoadingMovies.value = false
            }
        }
    }

    fun searchMovies(query: String) {
        viewModelScope.launch {
            try {
                _isLoadingMovies.value = true
                val results = repository.searchMovies(query)
                _searchResult.value = results
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error searching movies: ${e.message}"
            } finally {
                _isLoadingMovies.value = false
            }
        }
    }

    fun clearUserMovies() {
        _favorites.value = emptyList()
        _watched.value = emptyList()
        _wantToWatch.value = emptyList()
        _isLoadingMovies.value = false
        Log.d("MovieViewModel", "Cleared all user movie lists.")
    }

    fun refreshUserMovies(userId: String) {
        if (userId.isEmpty()) {
            Log.e("MovieViewModel", "Cannot refresh user movies: userId is empty.")
            clearUserMovies()
            return
        }

        _isLoadingMovies.value = true
        Log.d("MovieViewModel", "Starting movie data refresh for user $userId.")

        viewModelScope.launch {
            try {
                repository.getFavoritesForUser(userId).collectLatest {
                    _favorites.value = it
                    Log.d("MovieViewModel", "Favorites for $userId updated: ${it.size} movies")
                }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error collecting favorite movies for $userId: ${e.message}")
            }
        }
        viewModelScope.launch {
            try {
                repository.getWatchedForUser(userId).collectLatest {
                    _watched.value = it
                    Log.d("MovieViewModel", "Watched for $userId updated: ${it.size} movies")
                }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error collecting watched movies for $userId: ${e.message}")
            }
        }
        viewModelScope.launch {
            try {
                repository.getWantToWatchForUser(userId).collectLatest {
                    _wantToWatch.value = it
                    Log.d("MovieViewModel", "WantToWatch for $userId updated: ${it.size} movies")
                }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error collecting want-to-watch movies for $userId: ${e.message}")
            } finally {
                _isLoadingMovies.value = false
                Log.d("MovieViewModel", "Finished attempting to refresh movie data for user $userId.")
            }
        }
    }

    fun removeMovie(userId: String, movie: MovieEntity, type: String) = viewModelScope.launch {
        try {
            val existingMovie = repository.getMovieByIdAndUserId(movie.tmdbMovieId, userId)

            if (existingMovie != null) {
                val movieAfterRemovalAttempt = when (type.lowercase()) {
                    "favorites" -> existingMovie.copy(isFavorite = false)
                    "watched" -> existingMovie.copy(isWatched = false)
                    "want to watch" -> existingMovie.copy(isWantToWatch = false)
                    else -> existingMovie
                }

                if (!movieAfterRemovalAttempt.isFavorite &&
                    !movieAfterRemovalAttempt.isWatched &&
                    !movieAfterRemovalAttempt.isWantToWatch
                ) {
                    repository.deleteMovie(movieAfterRemovalAttempt)
                    Log.d("MovieViewModel", "Deleted movie ${movie.title} as all flags are now false.")
                } else {
                    repository.insertMovie(movieAfterRemovalAttempt)
                    Log.d("MovieViewModel", "Removed flag '$type' from movie ${movie.title}. Remaining state: $movieAfterRemovalAttempt")
                }
                refreshUserMovies(userId)
            } else {
                Log.d("MovieViewModel", "Attempted to remove flag '$type' from non-existent movie for user $userId: ${movie.title}")
            }
        } catch (e: Exception) {
            Log.e("MovieViewModel", "Error removing flag '$type' for movie ${movie.title}: ${e.localizedMessage}")
        }
    }


    fun toggleFlag(
        userId: String,
        movie: MovieEntity,
        type: String
    ) = viewModelScope.launch {
        try {
            val existingMovie = repository.getMovieByIdAndUserId(movie.tmdbMovieId, userId)

            val movieToSave: MovieEntity = if (existingMovie != null) {
                existingMovie.copy(
                    isFavorite = if (type == "favorite") !existingMovie.isFavorite else existingMovie.isFavorite,
                    isWatched = if (type == "watched") !existingMovie.isWatched else existingMovie.isWatched,
                    isWantToWatch = if (type == "wantToWatch") !existingMovie.isWantToWatch else existingMovie.isWantToWatch
                )
            } else {
                val assignedListType = when (type) {
                    "favorite" -> "favorites"
                    "watched" -> "watched"
                    "wantToWatch" -> "want to watch"
                    else -> "unknown"
                }

                movie.copy(
                    userId = userId,
                    isFavorite = (type == "favorite"),
                    isWatched = (type == "watched"),
                    isWantToWatch = (type == "wantToWatch"),
                    listType = assignedListType
                )
            }

            repository.insertMovie(movieToSave)
            refreshUserMovies(userId)

        } catch (e: Exception) {
            Log.e("MovieViewModel", "Error toggling flag '$type' for movie ${movie.title}: ${e.localizedMessage}")
        }
    }

    fun deleteAllMoviesForUser(userId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAllMoviesByUserId(userId)
                refreshUserMovies(userId)
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error deleting all user movies: ${e.message}")
            }
        }
    }
}