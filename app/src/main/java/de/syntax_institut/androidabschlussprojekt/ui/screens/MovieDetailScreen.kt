package de.syntax_institut.androidabschlussprojekt.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import de.syntax_institut.androidabschlussprojekt.data.database.MovieEntity
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.MovieViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    navController: NavController,
    movieId: Int,
    posterPath: String?,
    title: String?,
    overview: String?,
    releaseDate: String?,
    authViewModel: AuthViewModel = koinViewModel(),
    movieViewModel: MovieViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val userId by authViewModel.currentUserId.collectAsState()

    val fullPosterUrl = if (!posterPath.isNullOrEmpty()) {
        "https://image.tmdb.org/t/p/w500/$posterPath"
    } else {
        "https://via.placeholder.com/500x750?text=No+Image"
    }

    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500), label = "alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 40f,
        animationSpec = tween(durationMillis = 500), label = "offsetY"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    val movieEntity = MovieEntity(
        id = 0,
        tmdbMovieId = movieId,
        userId = userId ?: "",
        title = title ?: "",
        posterPath = posterPath ?: "",
        overview = overview ?: "",
        releaseDate = releaseDate ?: "",
        listType = ""
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title ?: "",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = offsetY
                }
        ) {
            AsyncImage(
                model = fullPosterUrl,
                contentDescription = "$title Poster",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title ?: "Unknown Title",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Release Date",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = releaseDate ?: "N/A",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = overview ?: "No overview available.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = {
                    if (isAuthenticated && userId != null) {
                        movieViewModel.toggleFlag(userId!!, movieEntity, "wantToWatch")
                        Toast.makeText(context, "Added to Want to Watch", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please log in first to use this feature", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Want to Watch")
                }

                OutlinedButton(onClick = {
                    if (isAuthenticated && userId != null) {
                        movieViewModel.toggleFlag(userId!!, movieEntity, "watched")
                        Toast.makeText(context, "Added to Watched", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please log in first to use this feature", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Watched")
                }

                IconButton(onClick = {
                    if (isAuthenticated && userId != null) {
                        movieViewModel.toggleFlag(userId!!, movieEntity, "favorite")
                        Toast.makeText(context, "Toggled Favorite", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please log in first to use this feature", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Add to Favorites",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}