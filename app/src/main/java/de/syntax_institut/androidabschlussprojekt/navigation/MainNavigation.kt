package de.syntax_institut.androidabschlussprojekt.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.ui.components.MainScaffold
import de.syntax_institut.androidabschlussprojekt.ui.screens.*
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainNavigation(
    navController: NavHostController,
    onNavigateToMovieDetail: (Movie) -> Unit,
    isDarkModeEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    settingsViewModel: SettingsViewModel
) {
    val authViewModel: AuthViewModel = koinViewModel()
    val settingsViewModel: SettingsViewModel = koinViewModel()

    val didLogout by authViewModel.didLogout.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val authReady by authViewModel.authReady.collectAsState()

    LaunchedEffect(didLogout) {
        if (didLogout) {
            Log.d("MainNavigation", "Logout detected. Navigating to AUTH_FLOW.")
            navController.navigate(Routes.AUTH) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
            authViewModel.clearLogoutFlag()
        }
    }

    if (!authReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            Text("Loading...")
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME
        ) {
            composable(Routes.HOME) {
                MainScaffold(navController = navController) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onMovieClick = onNavigateToMovieDetail,
                        isAuthenticated = isAuthenticated,
                        onProfileClick = { navController.navigate(Routes.AUTH) },
                        onSearchClick = { navController.navigate(Routes.SEARCH) }
                    )
                }
            }

            composable(Routes.SEARCH) {
                MainScaffold(navController = navController) { innerPadding ->
                    SearchScreen(
                        modifier = Modifier.padding(innerPadding),
                        onMovieClick = onNavigateToMovieDetail
                    )
                }
            }

            composable(
                route = Routes.MOVIE_DETAIL,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType },
                    navArgument("posterPath") { type = NavType.StringType; nullable = true },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("overview") { type = NavType.StringType },
                    navArgument("releaseDate") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId")
                val posterPath = backStackEntry.arguments?.getString("posterPath")?.decodeURLPath()
                val title = backStackEntry.arguments?.getString("title")?.decodeURLPath()
                val overview = backStackEntry.arguments?.getString("overview")?.decodeURLPath()
                val releaseDate = backStackEntry.arguments?.getString("releaseDate")?.decodeURLPath()

                if (movieId != null) {
                    MovieDetailScreen(
                        navController = navController,
                        posterPath = posterPath,
                        movieId = movieId,
                        title = title,
                        overview = overview,
                        releaseDate = releaseDate
                    )
                } else {
                    Log.e("MainNavigation", "Movie ID is null for MovieDetailScreen.")
                    navController.popBackStack()
                }
            }

            composable(Routes.AUTH) {
                MainScaffold(navController = navController) { innerPadding ->
                    if (isAuthenticated) {
                        ProfileScreen(
                            modifier = Modifier.padding(innerPadding),
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    } else {
                        AuthScreen(
                            modifier = Modifier.padding(innerPadding),
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.AUTH) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    navController = navController,
                    isDarkModeEnabled = isDarkModeEnabled,
                    onToggleDarkMode = onToggleDarkMode,
                    settingsViewModel = settingsViewModel,
                    onEditNickname = {},
                    onChangeProfilePicture = {},
                    onChangePassword = {},
                    onDeleteAccount = {},
                    onLogout = { authViewModel.signOut() }
                )
            }
        }
    }
}