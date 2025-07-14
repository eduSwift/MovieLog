package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import de.syntax_institut.androidabschlussprojekt.data.database.MovieEntity
import de.syntax_institut.androidabschlussprojekt.ui.state.UiState
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.MovieViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel = koinViewModel(),
    movieViewModel: MovieViewModel = koinViewModel()
) {
    val userState by authViewModel.userState.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()

    val favorites by movieViewModel.favorites.collectAsState()
    val watched by movieViewModel.watched.collectAsState()
    val wantToWatch by movieViewModel.wantToWatch.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCompleteProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isAuthenticated, currentUserId) {
        if (isAuthenticated) {
            currentUserId?.let { uid ->
                authViewModel.loadUserData(uid)
                movieViewModel.refreshUserMovies(uid)
            }
        } else {
            authViewModel.clearUserData()
            movieViewModel.clearUserMovies()
        }
    }

    when (val state = userState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFB3D7EA)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFB3D7EA)),
                contentAlignment = Alignment.Center
            ) {
                Text("Failed to load profile: ${state.message}", color = Color.Red)
            }
        }

        is UiState.Success -> {
            val user = state.data
            val shouldPromptToCompleteProfile = user.nickname.startsWith("User_")

            LaunchedEffect(shouldPromptToCompleteProfile) {
                if (shouldPromptToCompleteProfile) {
                    showCompleteProfileDialog = true
                }
            }

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFFB3D7EA))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = user.profileImageUrl,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = user.nickname,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }

                    if (favorites.isNotEmpty()) {
                        item {
                            MovieListSection("Favorites", favorites, user.uid, movieViewModel, "favorite")
                        }
                    }

                    if (wantToWatch.isNotEmpty()) {
                        item {
                            MovieListSection("Want to Watch", wantToWatch, user.uid, movieViewModel, "wantToWatch")
                        }
                    }

                    if (watched.isNotEmpty()) {
                        item {
                            MovieListSection("Watched", watched, user.uid, movieViewModel, "watched")
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }

                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Logout")
                }
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

            if (showCompleteProfileDialog) {
                var nickname by remember { mutableStateOf(user.nickname) }

                AlertDialog(
                    onDismissRequest = { showCompleteProfileDialog = false },
                    title = { Text("Complete Your Profile") },
                    text = {
                        Column {
                            Text("Set your nickname:")
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = nickname,
                                onValueChange = { nickname = it },
                                label = { Text("Nickname") }
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("(Profile image selection coming soon!)")
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                authViewModel.updateNickname(user.uid, nickname.trim())
                                showCompleteProfileDialog = false
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCompleteProfileDialog = false }) {
                            Text("Skip")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MovieListSection(
    title: String,
    movies: List<MovieEntity>,
    userId: String,
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

        movies.forEach { movie ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w92/${movie.posterPath}",
                        contentDescription = "${movie.title} Poster",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(movie.title, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = movie.releaseDate,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color.Gray
                        )
                    }

                    IconButton(onClick = {
                        viewModel.removeMovie(userId, movie, type)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
        }
    }
}