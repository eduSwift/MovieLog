package de.syntax_institut.androidabschlussprojekt.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import de.syntax_institut.androidabschlussprojekt.data.model.Movie

@Composable
fun MovieCardItem(
    movie: Movie,
    onClick: () -> Unit
) {
    val imageUrl = "https://image.tmdb.org/t/p/w500/${movie.poster_path}"
    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500), label = "alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(durationMillis = 500), label = "scale"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Card(
        modifier = Modifier
            .padding(end = 12.dp)
            .width(160.dp)
            .scale(scale)
            .alpha(alpha)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8))
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Poster for ${movie.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                text = movie.title,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(8.dp),
                maxLines = 2
            )
        }
    }
}
