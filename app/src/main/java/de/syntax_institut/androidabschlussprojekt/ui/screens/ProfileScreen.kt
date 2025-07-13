package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import de.syntax_institut.androidabschlussprojekt.data.database.MovieEntity
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
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUserIdState = authViewModel.currentUserId.collectAsState()
    val userEntityState = authViewModel.userEntity.collectAsState()

    val favorites by movieViewModel.favorites.collectAsState()
    val watched by movieViewModel.watched.collectAsState()
    val wantToWatch by movieViewModel.wantToWatch.collectAsState()

    val currentUserId = currentUserIdState.value
    val userEntity = userEntityState.value

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCompleteProfileDialog by remember { mutableStateOf(false) }
    var userDataLoadInitiated by remember { mutableStateOf(false) }
    var movieDataLoadInitiated by remember { mutableStateOf(false) }

    val shouldPromptToCompleteProfile = remember(userEntity) {
        userEntity?.nickname?.startsWith("User_") == true
    }

    LaunchedEffect(isAuthenticated, currentUserId) {
        if (isAuthenticated && currentUserId != null) {
            if (!userDataLoadInitiated || userEntity?.uid != currentUserId) {
                authViewModel.loadUserData(currentUserId)
                userDataLoadInitiated = true
            }

            if (!movieDataLoadInitiated) {
                movieViewModel.refreshUserMovies(currentUserId)
                movieDataLoadInitiated = true
            }
        } else {
            userDataLoadInitiated = false
            movieDataLoadInitiated = false
            authViewModel.clearUserData()
            movieViewModel.clearUserMovies()
        }
    }

    LaunchedEffect(shouldPromptToCompleteProfile) {
        if (shouldPromptToCompleteProfile) {
            showCompleteProfileDialog = true
        }
    }

    val isUserProfileLoading = isAuthenticated && userEntity == null
    val isMovieListsLoading = isAuthenticated && movieDataLoadInitiated &&
            favorites.isEmpty() && watched.isEmpty() && wantToWatch.isEmpty()
    val isLoadingUI = !isAuthenticated || currentUserId == null || isUserProfileLoading || isMovieListsLoading

    if (isLoadingUI) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFB3D7EA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (!isAuthenticated || currentUserId == null || userEntity == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFB3D7EA)), contentAlignment = Alignment.Center) {
            Text("Please log in to view your profile.", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
        }
        return
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
                            model = userEntity.profileImageUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = userEntity.nickname,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            if (favorites.isNotEmpty()) {
                item {
                    MovieListSection("Favorites", favorites, currentUserId, movieViewModel, "favorite")
                }
            }

            if (wantToWatch.isNotEmpty()) {
                item {
                    MovieListSection("Want to Watch", wantToWatch, currentUserId, movieViewModel, "wantToWatch")
                }
            }

            if (watched.isNotEmpty()) {
                item {
                    MovieListSection("Watched", watched, currentUserId, movieViewModel, "watched")
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
        var nickname by remember { mutableStateOf(userEntity.nickname) }

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
                        authViewModel.updateNickname(userEntity.uid, nickname.trim())
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
