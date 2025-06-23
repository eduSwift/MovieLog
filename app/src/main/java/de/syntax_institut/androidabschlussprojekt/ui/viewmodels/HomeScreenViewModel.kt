package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import kotlinx.coroutines.flow.asStateFlow

class HomeScreenViewModel: ViewModel() {
    private val _movies = MutableStateFlow(listOf<Movie>())
    val movies = _movies.asStateFlow()
}