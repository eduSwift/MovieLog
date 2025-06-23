package de.syntax_institut.androidabschlussprojekt.ui.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import androidx.compose.ui.Modifier


@Composable
fun MovieList(
    modifier: Modifier = Modifier,
    movies: List<Movie>
) {

    LazyColumn(modifier = modifier) {
        items(movies) { movie ->
            MovieItem(movie)
        }
    }
}