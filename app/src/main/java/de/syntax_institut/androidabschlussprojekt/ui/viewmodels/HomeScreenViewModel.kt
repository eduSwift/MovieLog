package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory
import de.syntax_institut.androidabschlussprojekt.data.repository.MovieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _allMoviesForCategory = MutableStateFlow<List<Movie>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(MovieCategory.POPULAR)
    val selectedCategory: StateFlow<MovieCategory> = _selectedCategory.asStateFlow()

    private val _navigateToMovieDetail = MutableStateFlow<Movie?>(null)
    val navigateToMovieDetail: StateFlow<Movie?> = _navigateToMovieDetail.asStateFlow()

    val movies: StateFlow<List<Movie>> = _allMoviesForCategory
        .combine(_searchQuery) { allMovies, query ->
            if (query.isBlank()) {
                allMovies
            } else {
                allMovies.filter { movie ->
                    movie.title.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(
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
