package de.syntax_institut.androidabschlussprojekt.ui.components

import androidx.compose.foundation.clickable // New import for clickable modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController // New import for NavController
import coil3.compose.AsyncImage
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import java.net.URLEncoder // For URL encoding
import java.nio.charset.StandardCharsets // For UTF-8 charset


fun String.encodeURLPath(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
        .replace("+", "%20")
}

@Composable
fun MovieItem(movie: Movie, navController: NavController) {

    Card(elevation = CardDefaults
        .cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(8.dp)) {

            val imageUrl = "https://image.tmdb.org/t/p/w500/${movie.poster_path}"
            println("Loading image from: $imageUrl")
            AsyncImage(
                model = imageUrl,
                contentDescription = "Movie Image",
                modifier = Modifier
                    .width(92.dp)
                    .clickable {
                        navController.navigate(
                            "movie_detail_route/" +
                                    "${movie.poster_path?.encodeURLPath()}/" +
                                    "${movie.title.encodeURLPath()}/" +
                                    "${movie.overview.encodeURLPath()}/" +
                                    "${movie.release_date.encodeURLPath()}"
                        )
                    },
                onLoading = { println("Loading image for ${movie.title}")},
                onError = { println("Error loading image: ${it.result.throwable}")},
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = movie.title, style = MaterialTheme.typography.labelLarge)

                Text(text = "Release Date", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Text(text = movie.release_date, style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.padding(top = 8.dp))

                Row {
                    Button(
                        onClick = { /* Handle "Watched It" click for movie.title */ },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text("Watched It")
                    }

                    OutlinedButton(
                        onClick = { /* Handle "Wishlist" click for movie.title */ },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text("Wishlist")
                    }

                    IconButton(onClick = { /* Handle "Favorite" click for movie.title */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Add to Favorites")
                    }
                }
            }
        }
    }
}