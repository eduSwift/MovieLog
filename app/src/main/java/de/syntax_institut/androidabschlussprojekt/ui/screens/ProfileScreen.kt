package de.syntax_institut.androidabschlussprojekt.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import de.syntax_institut.androidabschlussprojekt.R
import de.syntax_institut.androidabschlussprojekt.data.database.MovieEntity
import de.syntax_institut.androidabschlussprojekt.navigation.Routes
import de.syntax_institut.androidabschlussprojekt.ui.state.UiState
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.MovieViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.ProfileViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel = koinViewModel(),
    movieViewModel: MovieViewModel = koinViewModel()
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val userState by profileViewModel.userState.collectAsState()
    val favorites by movieViewModel.favorites.collectAsState()
    val watched by movieViewModel.watched.collectAsState()
    val wantToWatch by movieViewModel.wantToWatch.collectAsState()
    val didDeleteAccount by authViewModel.didDeleteAccount.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            currentUserId?.let { uid -> profileViewModel.uploadProfileImage(uid, it) }
        }
    }

    var showProfileSetupDialog by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }

    // Navigate to Auth screen after deletion
    LaunchedEffect(didDeleteAccount) {
        if (didDeleteAccount) {
            navController.navigate(Routes.AUTH) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            authViewModel.clearDeleteFlag()
        }
    }

    // Load data and check profile completeness immediately
    LaunchedEffect(currentUserId) {
        currentUserId?.let {
            profileViewModel.loadUserData(it)
            movieViewModel.refreshUserMovies(it)
        }
    }

    LaunchedEffect(userState) {
        if (userState is UiState.Success) {
            val user = (userState as UiState.Success).data
            nickname = user.nickname
            // Only show dialog if profile is not complete AND user is authenticated
            // This prevents the dialog from briefly showing on initial load if user is not yet logged in
            if (!user.isProfileComplete && isAuthenticated) {
                showProfileSetupDialog = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {
                    navController.navigate(Routes.SETTINGS)
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (userState) {
                    is UiState.Success -> {
                        val user = (userState as UiState.Success).data

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = user.profileImageUrl.takeIf { !it.isNullOrBlank() },
                                contentDescription = "Profile Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                placeholder = painterResource(id = R.drawable.ic_default_profile_placeholder),
                                error = painterResource(id = R.drawable.ic_default_profile_placeholder)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = user.nickname.ifBlank { "Your nickname" },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    is UiState.Error, UiState.Loading -> {
                        // Show fallback content while loading or on error
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = null,
                                contentDescription = "Default Profile Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                placeholder = painterResource(id = R.drawable.ic_default_profile_placeholder),
                                error = painterResource(id = R.drawable.ic_default_profile_placeholder)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Welcome!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (userState is UiState.Error && isAuthenticated) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Error loading your profile.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp
                                )
                            }

                            if (userState is UiState.Loading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            MovieListSection("Favorites", favorites, currentUserId, movieViewModel)
            MovieListSection("Watched", watched, currentUserId, movieViewModel)
            MovieListSection("Want to Watch", wantToWatch, currentUserId, movieViewModel)

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showProfileSetupDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Complete Your Profile", style = MaterialTheme.typography.titleLarge)

                        Spacer(modifier = Modifier.height(12.dp))

                        AsyncImage(
                            model = null, // Current profile image can be shown here if already uploaded
                            contentDescription = "Select Profile Image",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            placeholder = painterResource(id = R.drawable.ic_default_profile_placeholder),
                            error = painterResource(id = R.drawable.ic_default_profile_placeholder)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text("Enter a nickname") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                currentUserId?.let {
                                    profileViewModel.updateNickname(it, nickname)
                                    profileViewModel.markProfileComplete(it)
                                }
                                showProfileSetupDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = { showProfileSetupDialog = false }) {
                            Text("Skip")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieListSection(
    title: String,
    movies: List<MovieEntity>,
    currentUserId: String?,
    movieViewModel: MovieViewModel
) {
    if (movies.isEmpty()) return

    Column {
        Text(
            text = "$title (${movies.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            movies.forEach { movie ->
                MovieCard(
                    movie = movie,
                    onClick = {},
                    onDelete = {
                        if (currentUserId != null) {
                            movieViewModel.removeMovie(currentUserId, movie, type = title.lowercase())
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: MovieEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val imageUrl = "https://image.tmdb.org/t/p/w500/${movie.posterPath}"

    Card(
        modifier = Modifier
            .padding(end = 12.dp)
            .width(140.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Poster for ${movie.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}