package de.syntax_institut.androidabschlussprojekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.navigation.MainNavigation
import de.syntax_institut.androidabschlussprojekt.navigation.Routes
import de.syntax_institut.androidabschlussprojekt.ui.theme.AndroidAbschlussprojektTheme
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AndroidAbschlussprojektTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel() // ✅ instantiate ViewModel

                MainNavigation(
                    navController = navController,
                    authViewModel = authViewModel, // ✅ pass it in
                    onNavigateToMovieDetail = { movie: Movie ->
                        val route = Routes.movieDetailRoute(
                            posterPath = movie.poster_path ?: "",
                            title = movie.title,
                            overview = movie.overview,
                            releaseDate = movie.release_date
                        )
                        navController.navigate(route)
                    }
                )
            }
        }
    }
}
