package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory
import de.syntax_institut.androidabschlussprojekt.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel(private val repository: MovieRepository) : ViewModel() {

    // Example: State for a list of popular movies for the home screen
    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularMovies: StateFlow<List<Movie>> = _popularMovies

    init {
        // Fetch initial data for the home screen (e.g., popular movies)
        fetchPopularMovies()
    }

    private fun fetchPopularMovies() {
        viewModelScope.launch {
            try {
                // You might have a different method in your repository for popular movies
                val movies = repository.getMoviesByCategory(MovieCategory.POPULAR) // Assuming POPULAR category exists
                _popularMovies.value = movies
            } catch (e: Exception) {
                Log.e("HomeScreenViewModel", "Error fetching popular movies: ${e.message}", e)
            }
        }
    }

    // Remove search-related properties and functions from here
    // as they are now in SearchScreenViewModel
    // private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    // val movies: StateFlow<List<Movie>> = _movies

    // private val _searchQuery = MutableStateFlow("")
    // val searchQuery: StateFlow<String> = _searchQuery

    // fun updateSearchQuery(query: String) { ... }
}