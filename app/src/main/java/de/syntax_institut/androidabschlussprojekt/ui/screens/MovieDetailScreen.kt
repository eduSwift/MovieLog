package de.syntax_institut.androidabschlussprojekt.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import de.syntax_institut.androidabschlussprojekt.data.database.UserEntity
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.CommentViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.MovieViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

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
    movieViewModel: MovieViewModel = koinViewModel(),
    commentViewModel: CommentViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val userId by authViewModel.currentUserId.collectAsState()
    val comments by commentViewModel.comments.collectAsState()
    val commentCount by commentViewModel.commentCount.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(500), label = "")
    val offsetY by animateFloatAsState(targetValue = if (visible) 0f else 40f, animationSpec = tween(500), label = "")

    var newCommentText by remember { mutableStateOf("") }
    var editingCommentId by remember { mutableStateOf<Int?>(null) }
    var editingText by remember { mutableStateOf("") }

    val fullPosterUrl = if (!posterPath.isNullOrEmpty()) {
        "https://image.tmdb.org/t/p/w500/$posterPath"
    } else {
        "https://via.placeholder.com/500x750?text=No+Image"
    }

    val userMap = remember { mutableStateMapOf<String, UserEntity?>() }

    LaunchedEffect(Unit) {
        visible = true
        commentViewModel.loadComments(movieId)
    }

    LaunchedEffect(comments) {
        comments.forEach { comment ->
            if (userMap[comment.userId] == null) {
                val user = commentViewModel.getUserById(comment.userId)
                userMap[comment.userId] = user
            }
        }
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
                title = { Text(text = title ?: "") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    this.translationY = offsetY
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
            Text(title ?: "Unknown Title", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Release Date", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(releaseDate ?: "N/A", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Overview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(overview ?: "No overview available.", style = MaterialTheme.typography.bodyLarge)

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
                    } else Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show()
                }) { Text("Want to Watch") }

                OutlinedButton(onClick = {
                    if (isAuthenticated && userId != null) {
                        movieViewModel.toggleFlag(userId!!, movieEntity, "watched")
                        Toast.makeText(context, "Added to Watched", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show()
                }) { Text("Watched") }

                IconButton(onClick = {
                    if (isAuthenticated && userId != null) {
                        movieViewModel.toggleFlag(userId!!, movieEntity, "favorite")
                        Toast.makeText(context, "Toggled Favorite", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Add to Favorites", tint = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comments",
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$commentCount Comment${if (commentCount == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Comments", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            comments.forEach { comment ->
                val isMine = comment.userId == userId
                val user = userMap[comment.userId]

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = user?.profileImageUrl ?: "",
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(user?.nickname ?: "Unknown", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(comment.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (editingCommentId == comment.id) {
                        OutlinedTextField(
                            value = editingText,
                            onValueChange = { editingText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Edit comment") }
                        )
                        Row(modifier = Modifier.align(Alignment.End)) {
                            TextButton(onClick = {
                                commentViewModel.updateComment(comment.copy(text = editingText))
                                editingCommentId = null
                                editingText = ""
                            }) {
                                Text("Save")
                            }
                            TextButton(onClick = {
                                editingCommentId = null
                                editingText = ""
                            }) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                        if (isMine) {
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = {
                                    editingCommentId = comment.id
                                    editingText = comment.text
                                }) { Text("Edit") }

                                TextButton(onClick = {
                                    commentViewModel.deleteComment(comment)
                                }) { Text("Delete", color = Color.Red) }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    label = { Text("Leave a comment...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (isAuthenticated && userId != null && newCommentText.isNotBlank()) {
                        commentViewModel.addComment(userId!!, movieId, newCommentText.trim())
                        newCommentText = ""
                    } else {
                        Toast.makeText(context, "Please log in to comment", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Send")
                }
            }
        }
    }
}