package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.ui.components.MovieCardItem
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.HomeScreenViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = koinViewModel(),
    onMovieClick: (Movie) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val moviesByCategory = viewModel.moviesByCategory.collectAsState()
    val backgroundColor = Color(0xFFB3D7EA)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        moviesByCategory.value.forEach { (category, movies) ->
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
                ) {
                    Text(
                        text = category.name.replace("_", " "),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                        textAlign = TextAlign.Start
                    )
                }
            }

            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 0.dp)) {
                    items(movies) { movie ->
                        MovieCardItem(
                            movie = movie,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
            }
        }
    }
}
