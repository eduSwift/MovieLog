package de.syntax_institut.androidabschlussprojekt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

    Card(
        modifier = Modifier
            .padding(end = 12.dp)
            .width(160.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
