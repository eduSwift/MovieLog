package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.repository.MovieRepository
import de.syntax_institut.androidabschlussprojekt.BuildConfig

class HomeScreenViewModel : ViewModel() {


    private val apiKey = BuildConfig.MOVIE_API_KEY

    private val repository = MovieRepository()

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies = _movies.asStateFlow()

    init {
        loadMovies()
    }

    private fun loadMovies() {
        viewModelScope.launch {
            try {
                val fetchedMovies = repository.getPopularMoviesFromOnlineApi(apiKey)
                _movies.value = fetchedMovies
            } catch (e: Exception) {
                println("Error loading movies: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
