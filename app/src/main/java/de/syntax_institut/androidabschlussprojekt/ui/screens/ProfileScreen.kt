package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import de.syntax_institut.androidabschlussprojekt.data.database.MovieEntity
import de.syntax_institut.androidabschlussprojekt.navigation.Routes
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.MovieViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isAuthenticated: Boolean,
    authViewModel: AuthViewModel = viewModel(),
    movieViewModel: MovieViewModel = koinViewModel()
) {
    val currentUserId by authViewModel.currentUserId.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val favorites by movieViewModel.favorites.collectAsState()
    val watched by movieViewModel.watched.collectAsState()
    val wantToWatch by movieViewModel.wantToWatch.collectAsState()

    LaunchedEffect(currentUserId) {
        currentUserId?.let { movieViewModel.refreshUserMovies(it) }
    }

    if (!isAuthenticated) {
        LaunchedEffect(Unit) {
            navController.navigate(Routes.PROFILE_ENTRY) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFB3D7EA))
            .padding(16.dp)
    ) {
        Text("Welcome!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("User ID: ${currentUserId ?: "Unknown"}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showLogoutDialog = true }) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(24.dp))

        MovieListSection("Favorites", favorites, currentUserId, movieViewModel, "favorite")
        MovieListSection("Want to Watch", wantToWatch, currentUserId, movieViewModel, "wantToWatch")
        MovieListSection("Watched", watched, currentUserId, movieViewModel, "watched")
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout Confirmation") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MovieListSection(
    title: String,
    movies: List<MovieEntity>,
    userId: String?,
    viewModel: MovieViewModel,
    type: String
) {
    if (movies.isNotEmpty()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            items(movies) { movie ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = movie.title, style = MaterialTheme.typography.bodyLarge)
                            Text(text = movie.releaseDate, style = MaterialTheme.typography.bodySmall)
                        }

                        IconButton(onClick = {
                            userId?.let {
                                viewModel.removeMovie(it, movie, type)
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }
        }
    }
}
