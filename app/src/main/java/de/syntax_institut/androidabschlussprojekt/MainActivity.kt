package de.syntax_institut.androidabschlussprojekt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.navigation.MainNavigation
import de.syntax_institut.androidabschlussprojekt.navigation.Routes
import de.syntax_institut.androidabschlussprojekt.ui.theme.AndroidAbschlussprojektTheme
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.NetworkStatusViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val isDarkModeEnabled by settingsViewModel.isDarkModeEnabled.collectAsState()
            val authViewModel: AuthViewModel =  getViewModel()

            val networkStatusViewModel: NetworkStatusViewModel = koinViewModel()
            val isOnline by networkStatusViewModel.isOnline.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            LaunchedEffect(isOnline) {
                if (!isOnline) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "No Internet Connection!",
                            actionLabel = "Dismiss",
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                } else {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }

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
                    authViewModel = authViewModel,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}