package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import de.syntax_institut.androidabschlussprojekt.ui.components.MovieList
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.HomeScreenViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel()
) {
    val moviesList by viewModel.movies.collectAsState()

    MovieList(movies = moviesList, modifier = modifier)
}