package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.ui.components.MovieList
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.SearchScreenViewModel
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchScreenViewModel = koinViewModel(),
    onMovieClick: (Movie) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var active by remember { mutableStateOf(true) }
    val backgroundColor = Color(0xFFB3D7EA)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = { active = false },
            active = active,
            onActiveChange = { active = it },
            placeholder = { Text("Search movies...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Search Results",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                MovieList(movies = searchResults, onMovieClick = onMovieClick)
            }
        }
    }
}

