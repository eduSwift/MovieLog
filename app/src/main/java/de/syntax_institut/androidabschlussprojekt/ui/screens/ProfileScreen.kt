package de.syntax_institut.androidabschlussprojekt.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
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

    val favorites by movieViewModel.favorites.collectAsState(initial = emptyList())
    val watched by movieViewModel.watched.collectAsState(initial = emptyList())
    val wantToWatch by movieViewModel.wantToWatch.collectAsState(initial = emptyList())

    val didDeleteAccount by authViewModel.didDeleteAccount.collectAsState()

    val ninaCloudinaryUrl = "https://res.cloudinary.com/dldlsfv1n/image/upload/v1752844361/nina_drdfkv.jpg"
    val avatarCloudinaryUrl = "https://res.cloudinary.com/dldlsfv1n/image/upload/v1752912339/avatar_z4obq5.jpg"

    var nickname by remember { mutableStateOf("") }
    var isEditingNickname by remember { mutableStateOf(false) }
    var movieToDelete by remember { mutableStateOf<MovieEntity?>(null) }
    var showProfileSetupDialog by remember { mutableStateOf(false) }


    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val uriString = it.toString()
            Log.d("PROFILE_URI", "Picked URI: $uriString")

            currentUserId?.let { uid ->
                profileViewModel.setProfileImageManually(uid, uriString)
                profileViewModel.loadUserData(uid)
            }
        }
    }

    LaunchedEffect(didDeleteAccount) {
        if (didDeleteAccount) {
            navController.navigate(Routes.AUTH) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            authViewModel.clearDeleteFlag()
        }
    }

    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            profileViewModel.loadUserData(userId)
            movieViewModel.refreshUserMovies(userId)
        } ?: run {
            movieViewModel.clearUserMovies()
        }
    }

    LaunchedEffect(userState, isAuthenticated) {
        if (userState is UiState.Success && isAuthenticated) {
            val user = (userState as UiState.Success).data
            nickname = user.nickname
            if (!user.isProfileComplete) {
                showProfileSetupDialog = true
            } else {
                showProfileSetupDialog = false
            }
        }
    }

    if (!isAuthenticated) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Redirecting to login...")
            }
        }
        LaunchedEffect(Unit) {
            navController.navigate(Routes.AUTH) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
        }
    } else {
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
                                val profileImageModel = user.profileImageUrl.takeIf { !it.isNullOrBlank() }

                                AsyncImage(
                                    model = profileImageModel,
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

                                if (isEditingNickname) {
                                    OutlinedTextField(
                                        value = nickname,
                                        onValueChange = { nickname = it },
                                        modifier = Modifier.fillMaxWidth(0.8f),
                                        singleLine = true,
                                        label = { Text("Edit Nickname") }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(horizontalArrangement = Arrangement.Center) {
                                        TextButton(onClick = {
                                            isEditingNickname = false
                                            currentUserId?.let { profileViewModel.updateNickname(it, nickname) }
                                        }) { Text("Save") }
                                        TextButton(onClick = { isEditingNickname = false }) { Text("Cancel") }
                                    }
                                } else {
                                    Text(
                                        text = user.nickname.ifBlank { "Your nickname" },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { isEditingNickname = true }
                                    )
                                }
                            }
                        }

                        is UiState.Error, UiState.Loading -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AsyncImage(
                                    model = null,
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

                                Text(
                                    text = if (userState is UiState.Loading) "Loading Profile..." else "Welcome!",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (userState is UiState.Error && isAuthenticated) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Error loading your profile. Please try again.",
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

                MovieListSection("Favorites", favorites, currentUserId, movieViewModel, navController) { movieToDelete = it }
                MovieListSection("Watched", watched, currentUserId, movieViewModel, navController) { movieToDelete = it }
                MovieListSection("Want to Watch", wantToWatch, currentUserId, movieViewModel, navController) { movieToDelete = it }

                Spacer(modifier = Modifier.height(32.dp))
            }

            movieToDelete?.let { movie ->
                AlertDialog(
                    onDismissRequest = { movieToDelete = null },
                    title = { Text("Delete Movie") },
                    text = { Text("Are you sure you want to delete '${movie.title}'?") },
                    confirmButton = {
                        TextButton(onClick = {
                            currentUserId?.let {
                                movieViewModel.removeMovie(it, movie, type = movie.listType)
                            }
                            movieToDelete = null
                        }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { movieToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
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
                            model = null,
                            contentDescription = "Select Profile Image",
                            contentScale = ContentScale.Crop,
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
                                currentUserId?.let { uid ->
                                    profileViewModel.updateNickname(uid, nickname)
                                    profileViewModel.markProfileComplete(uid)
                                }
                                showProfileSetupDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = {
                            currentUserId?.let { uid ->
                                profileViewModel.markProfileComplete(uid)
                            }
                            showProfileSetupDialog = false
                        }) {
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
    movieViewModel: MovieViewModel,
    navController: NavController,
    onConfirmDelete: (MovieEntity) -> Unit
) {
    Column {
        Text(
            text = "$title (${movies.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        if (movies.isEmpty()) {
            Text(
                text = "No movies in this list yet.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                movies.forEach { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = {
                            navController.navigate(
                                Routes.movieDetailRoute(
                                    movieId = movie.tmdbMovieId,
                                    posterPath = movie.posterPath ?: "",
                                    title = movie.title,
                                    overview = movie.overview,
                                    releaseDate = movie.releaseDate
                                )
                            )
                        },
                        onDelete = { onConfirmDelete(movie.copy(listType = title.lowercase())) }
                    )
                }
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