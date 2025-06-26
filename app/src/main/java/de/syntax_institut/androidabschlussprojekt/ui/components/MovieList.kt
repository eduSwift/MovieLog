package de.syntax_institut.androidabschlussprojekt.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.syntax_institut.androidabschlussprojekt.data.model.Movie

@Composable
fun MovieList(
    modifier: Modifier = Modifier,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(movies) { movie ->
            MovieItem(movie = movie, onMovieClick = onMovieClick)
        }
    }
}