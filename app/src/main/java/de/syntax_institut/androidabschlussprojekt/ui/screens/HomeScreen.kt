package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory
import de.syntax_institut.androidabschlussprojekt.ui.components.AppLogoHeader
import de.syntax_institut.androidabschlussprojekt.ui.components.MovieList
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
    val moviesList by viewModel.movies.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFFB3D7EA)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        AppLogoHeader()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category: ${selectedCategory.name.replace("_", " ")}",
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select category")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                MovieCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name.replace("_", " ")) },
                        onClick = {
                            viewModel.updateSelectedCategory(category)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        MovieList(movies = moviesList, onMovieClick = onMovieClick)
    }
}
