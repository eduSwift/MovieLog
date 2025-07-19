package de.syntax_institut.androidabschlussprojekt.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.syntax_institut.androidabschlusprojekt.ui.components.MainScaffold
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.ui.screens.AuthScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.HomeScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.MovieDetailScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.ProfileScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.SearchScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.SettingsScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.SplashScreen
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.ProfileViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainNavigation(
    navController: NavHostController,
    onNavigateToMovieDetail: (Movie) -> Unit,
    isDarkModeEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel
) {
    val profileViewModel: ProfileViewModel = koinViewModel()
    val didLogout by authViewModel.didLogout.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    LaunchedEffect(didLogout) {
        if (didLogout) {
            navController.navigate(Routes.AUTH) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
            authViewModel.clearLogoutFlag()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(navController = navController)
        }

        composable(Routes.HOME) {
            MainScaffold(
                navController = navController,
                isAuthenticated = isAuthenticated
            ) { innerPadding ->
                HomeScreen(
                    modifier = Modifier.padding(innerPadding),
                    onMovieClick = onNavigateToMovieDetail,
                    isAuthenticated = isAuthenticated,
                    onProfileClick = {
                        if (isAuthenticated) {
                            navController.navigate(Routes.PROFILE) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate(Routes.AUTH) {
                                popUpTo(Routes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onSearchClick = {
                        navController.navigate(Routes.SEARCH) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }

        composable(Routes.SEARCH) {
            MainScaffold(
                navController = navController,
                isAuthenticated = isAuthenticated
            ) { innerPadding ->
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
            MainScaffold(
                navController = navController,
                isAuthenticated = isAuthenticated
            ) { innerPadding ->
                AuthScreen(
                    modifier = Modifier.padding(innerPadding),
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Routes.PROFILE) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onSignUpSuccess = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }


        composable(Routes.PROFILE) {
            MainScaffold(
                navController = navController,
                isAuthenticated = isAuthenticated
            ) { innerPadding ->
                ProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                navController = navController,
                isDarkModeEnabled = isDarkModeEnabled,
                onToggleDarkMode = onToggleDarkMode,
                settingsViewModel = settingsViewModel,
                onChangePassword = {},
                onDeleteAccount = {},
                onLogout = { authViewModel.signOut() }
            )
        }
    }
}