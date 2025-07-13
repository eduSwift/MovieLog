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

// *** FIX 1: Change constructor to use MovieRepository ***
class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    // StateFlows for API movie lists
    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _searchResult = MutableStateFlow<List<Movie>>(emptyList())
    val searchResult: StateFlow<List<Movie>> = _searchResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // StateFlows for database movie lists (MovieEntity)
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
                _isLoadingMovies.value = true // Set loading for API calls as well
                val fetchedMovies = repository.getMoviesByCategory(category)
                _movies.value = fetchedMovies
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error fetching movies: ${e.message}"
            } finally {
                _isLoadingMovies.value = false // Reset loading
            }
        }
    }

    fun searchMovies(query: String) {
        viewModelScope.launch {
            try {
                _isLoadingMovies.value = true // Set loading for API calls as well
                val results = repository.searchMovies(query)
                _searchResult.value = results
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error searching movies: ${e.message}"
            } finally {
                _isLoadingMovies.value = false // Reset loading
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

    // *** FIX 2: Collect from MovieRepository's flows and manage loading state ***
    fun refreshUserMovies(userId: String) {
        if (userId.isEmpty()) {
            Log.e("MovieViewModel", "Cannot refresh user movies: userId is empty.")
            clearUserMovies()
            return
        }

        _isLoadingMovies.value = true
        Log.d("MovieViewModel", "Starting movie data refresh for user $userId.")

        // Launch separate coroutines for each flow collection
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

    // *** FIX 3: Ensure 'movie' parameter is MovieEntity, and use repository ***
    fun toggleFlag(
        userId: String,
        movie: MovieEntity, // <-- This MUST be MovieEntity to match MovieDetailScreen
        type: String
    ) = viewModelScope.launch {
        try {
            // Find the movie entity in the local database for this specific user.
            // *** FIX 4: Use repository.getMovieByIdAndUserId and movie.tmdbMovieId ***
            val existingMovie = repository.getMovieByIdAndUserId(movie.tmdbMovieId, userId)

            val movieToSave: MovieEntity

            if (existingMovie != null) {
                // If existing, update its flags
                movieToSave = existingMovie.copy(
                    isFavorite = if (type == "favorite") !existingMovie.isFavorite else existingMovie.isFavorite,
                    isWatched = if (type == "watched") !existingMovie.isWatched else existingMovie.isWatched,
                    isWantToWatch = if (type == "wantToWatch") !existingMovie.isWantToWatch else existingMovie.isWantToWatch
                )
                Log.d("MovieViewModel", "Toggled flag '$type' for existing movie ${movieToSave.title}. New state: $movieToSave")
            } else {
                // If not found, create a new MovieEntity using the one passed from the detail screen
                // The 'id' in 'movie' is 0, so Room will auto-generate it.
                movieToSave = movie.copy(
                    isFavorite = (type == "favorite"),
                    isWatched = (type == "watched"),
                    isWantToWatch = (type == "wantToWatch")
                )
                Log.d("MovieViewModel", "Inserting new movie with flag '$type': ${movie.title}. Initial state: $movieToSave")
            }

            // *** FIX 5: Use repository.insertMovie (handles both insert and update) ***
            repository.insertMovie(movieToSave)
            refreshUserMovies(userId)
        } catch (e: Exception) {
            Log.e("MovieViewModel", "Error toggling flag '$type' for movie ${movie.title}: ${e.localizedMessage}")
        }
    }

    // *** FIX 6: Use repository.getMovieByIdAndUserId and repository.deleteMovie ***
    fun removeMovie(userId: String, movie: MovieEntity, type: String) = viewModelScope.launch {
        try {
            val existingMovie = repository.getMovieByIdAndUserId(movie.tmdbMovieId, userId) // Use movie.tmdbMovieId here

            if (existingMovie != null) {
                val movieAfterRemovalAttempt = when (type) {
                    "favorite" -> existingMovie.copy(isFavorite = false)
                    "watched" -> existingMovie.copy(isWatched = false)
                    "wantToWatch" -> existingMovie.copy(isWantToWatch = false)
                    else -> existingMovie
                }

                if (!movieAfterRemovalAttempt.isFavorite &&
                    !movieAfterRemovalAttempt.isWatched &&
                    !movieAfterRemovalAttempt.isWantToWatch
                ) {
                    repository.deleteMovie(movieAfterRemovalAttempt)
                    Log.d("MovieViewModel", "Deleted movie ${movie.title} as all flags are now false.")
                } else {
                    // *** FIX 7: Use repository.insertMovie to update the existing entity ***
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

    fun deleteMovie(movie: MovieEntity) { // This function was missing its implementation in the provided snippet
        viewModelScope.launch {
            try {
                repository.deleteMovie(movie)
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error deleting single movie: ${e.message}")
            }
        }
    }

    fun deleteAllMoviesForUser(userId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAllMoviesByUserId(userId)
                refreshUserMovies(userId) // Refresh lists after deletion
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error deleting all user movies: ${e.message}")
            }
        }
    }
}