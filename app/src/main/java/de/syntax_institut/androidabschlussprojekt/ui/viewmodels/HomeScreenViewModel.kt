package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.repository.MovieRepository
import de.syntax_institut.androidabschlussprojekt.BuildConfig
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory

class HomeScreenViewModel : ViewModel() {
    private val apiKey = BuildConfig.MOVIE_API_KEY
    private val repository = MovieRepository()

    private val _allMoviesForCategory = MutableStateFlow<List<Movie>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MovieCategory.POPULAR)
    val selectedCategory: StateFlow<MovieCategory> = _selectedCategory.asStateFlow()

    private val _navigateToMovieDetail = MutableStateFlow<Movie?>(null)
    val navigateToMovieDetail: StateFlow<Movie?> = _navigateToMovieDetail.asStateFlow()

    val movies: StateFlow<List<Movie>> = _allMoviesForCategory.combine(_searchQuery) { allMovies, query ->
        if (query.isBlank()) {
            allMovies
        } else {
            allMovies.filter { movie ->
                movie.title.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            _selectedCategory.collect { category ->
                loadMovies(category)
            }
        }
    }

    private fun loadMovies(category: MovieCategory) {
        viewModelScope.launch {
            try {
                val fetchedMovies = repository.getMoviesByCategory(category)
                _allMoviesForCategory.value = fetchedMovies
            } catch (e: Exception) {
                println("Error loading movies for category ${category.name}: ${e.message}")
                e.printStackTrace()
                _allMoviesForCategory.value = emptyList()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(category: MovieCategory) {
        _selectedCategory.value = category
    }

    fun setMovieToNavigateTo(movie: Movie) {
        _navigateToMovieDetail.value = movie
    }

    fun onNavigationCompleted() {
        _navigateToMovieDetail.value = null
    }
}