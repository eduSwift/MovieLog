package de.syntax_institut.androidabschlussprojekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.navigation.MainNavigation
import de.syntax_institut.androidabschlussprojekt.navigation.Routes
import de.syntax_institut.androidabschlussprojekt.ui.theme.AndroidAbschlussprojektTheme
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val isDarkModeEnabled by settingsViewModel.isDarkModeEnabled.collectAsState()
            val authViewModel: AuthViewModel =  getViewModel()

            AndroidAbschlussprojektTheme(darkTheme = isDarkModeEnabled) {
                val navController = rememberNavController()

                MainNavigation(
                    navController = navController,
                    onNavigateToMovieDetail = { movie: Movie ->
                        val route = Routes.movieDetailRoute(
                            movieId = movie.id,
                            posterPath = movie.poster_path ?: "",
                            title = movie.title,
                            overview = movie.overview,
                            releaseDate = movie.release_date
                        )
                        navController.navigate(route)
                    },
                    isDarkModeEnabled = isDarkModeEnabled,
                    onToggleDarkMode = { enabled -> settingsViewModel.setDarkModeEnabled(enabled) },
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}