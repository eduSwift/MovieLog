package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory
import de.syntax_institut.androidabschlussprojekt.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _moviesByCategory = MutableStateFlow<Map<MovieCategory, List<Movie>>>(emptyMap())
    val moviesByCategory: StateFlow<Map<MovieCategory, List<Movie>>> = _moviesByCategory.asStateFlow()

    private val _navigateToMovieDetail = MutableStateFlow<Movie?>(null)
    val navigateToMovieDetail: StateFlow<Movie?> = _navigateToMovieDetail.asStateFlow()

    init {
        loadAllCategories()
    }

    private fun loadAllCategories() {
        viewModelScope.launch {
            val categoryMovieMap = mutableMapOf<MovieCategory, List<Movie>>()

            for (category in MovieCategory.entries) {
                try {
                    val movies = repository.getMoviesByCategory(category)
                    categoryMovieMap[category] = movies
                } catch (e: Exception) {
                    println("Error loading ${category.name}: ${e.message}")
                    categoryMovieMap[category] = emptyList()
                }
            }

            _moviesByCategory.value = categoryMovieMap
        }
    }

    fun setMovieToNavigateTo(movie: Movie) {
        _navigateToMovieDetail.value = movie
    }

    fun onNavigationCompleted() {
        _navigateToMovieDetail.value = null
    }
}
