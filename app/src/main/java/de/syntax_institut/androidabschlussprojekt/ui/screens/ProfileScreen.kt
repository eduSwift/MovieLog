// de.syntax_institut.androidabschlussprojekt.ui.screens.ProfileScreen

package de.syntax_institut.androidabschlussprojekt.ui.screens

import android.util.Log
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
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val userEntity by authViewModel.userEntity.collectAsState()
    val favorites by movieViewModel.favorites.collectAsState()
    val watched by movieViewModel.watched.collectAsState()
    val wantToWatch by movieViewModel.wantToWatch.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    val hasLoadedUserData = remember { mutableStateOf(false) }
    val hasLoadedMovieData = remember { mutableStateOf(false) }


    LaunchedEffect(isAuthenticated, currentUserId) {
        Log.d("ProfileScreen", "LaunchedEffect(isAuthenticated, currentUserId) triggered: isAuth=$isAuthenticated, uid=$currentUserId")

        if (isAuthenticated && currentUserId != null) {
            if (!hasLoadedUserData.value || authViewModel.userEntity.value?.uid != currentUserId) {
                Log.d("ProfileScreen", "Calling authViewModel.loadUserData for $currentUserId")
                authViewModel.loadUserData(currentUserId!!)
                hasLoadedUserData.value = true
            }


            if (!hasLoadedMovieData.value) {
                Log.d("ProfileScreen", "Calling movieViewModel.refreshUserMovies for $currentUserId")
                movieViewModel.refreshUserMovies(currentUserId!!)
                hasLoadedMovieData.value = true
            }

        } else {
            // Reset flags if not authenticated, so data loads on next login
            hasLoadedUserData.value = false
            hasLoadedMovieData.value = false
            Log.d("ProfileScreen", "User not authenticated, resetting data load flags.")
        }
    }

    // --- Logging for debugging purposes ---
    LaunchedEffect(userEntity) {
        Log.d("ProfileScreen", "UserEntity state updated: ${userEntity?.nickname ?: "NULL"}")
        if (userEntity != null) {
            // If userEntity becomes non-null, ensure the hasLoadedUserData flag is correctly set
            hasLoadedUserData.value = true
        }
    }
    LaunchedEffect(favorites, watched, wantToWatch) {
        Log.d("ProfileScreen", "Movie lists state updated: Fav=${favorites.size}, Watched=${watched.size}, WantToWatch=${wantToWatch.size}")
        // If any of these lists are updated (even if empty), it means the movie data has been 'loaded'
        // This is the key change for the movie data loading state
        if (hasLoadedMovieData.value && (favorites.isNotEmpty() || watched.isNotEmpty() || wantToWatch.isNotEmpty() ||
                    (favorites.isEmpty() && watched.isEmpty() && wantToWatch.isEmpty()))) {
            // This condition is tricky. A simpler approach is just:
            // if (hasLoadedMovieData.value) { // We already triggered refresh, so it's loaded
            //      // The fact that the Flow emitted means data was processed
            // }
            // For now, let's assume the refreshUserMovies call reliably updates the flows,
            // so we can trust `hasLoadedMovieData` to tell us if we tried to load.
        }
    }


    // --- Refined Display Loading State ---
    // The loading condition should check if:
    // 1. We are authenticated.
    // 2. We have a userId.
    // 3. The userEntity has been loaded (i.e., it's not null AND we initiated its load).
    // 4. The movie lists have been loaded (i.e., we initiated their load, and they are no longer in their *initial* empty state if they were supposed to be populated).

    val isUserProfileDataLoading = !isAuthenticated || currentUserId == null || userEntity == null
    val isMovieDataLoading = !isAuthenticated || currentUserId == null || !hasLoadedMovieData.value

    // Determine overall loading status. We are "loading" until *both* user profile and movie data are confirmed loaded.
    val isLoading = isUserProfileDataLoading || isMovieDataLoading

    Log.d("ProfileScreen-LoadingCheck",
        "IsLoading: $isLoading | " +
                "isUserProfileDataLoading: $isUserProfileDataLoading (isAuth=$isAuthenticated, uid=$currentUserId, userEntity=${userEntity?.nickname ?: "NULL"}) | " +
                "isMovieDataLoading: $isMovieDataLoading (hasLoadedMovieData=${hasLoadedMovieData.value}, favSize=${favorites.size}, watchedSize=${watched.size}, wantToWatchSize=${wantToWatch.size})"
    )


    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFB3D7EA)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return // Exit the Composable to show loading state
    }

    // --- Main Profile Content ---
    // This part only renders if all conditions for displaying data are met
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
                userEntity?.let { user ->
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

                            Text(
                                text = user.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Only show sections if there are movies in them
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
}

// MovieListSection remains unchanged and correct.
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
                        userId?.let { viewModel.removeMovie(it, movie, type) }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
        }
    }
}