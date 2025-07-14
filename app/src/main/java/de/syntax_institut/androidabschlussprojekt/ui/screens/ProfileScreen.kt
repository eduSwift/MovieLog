package de.syntax_institut.androidabschlussprojekt.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        val uid = currentUserId
        if (uri != null && uid != null) {
            authViewModel.uploadProfileImage(uid, uri)
        }
    }

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
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                        .clickable { launcher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = user.profileImageUrl,
                                        contentDescription = "Profile picture",
                                        modifier = Modifier.matchParentSize()
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .padding(4.dp)
                                    )
                                }

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