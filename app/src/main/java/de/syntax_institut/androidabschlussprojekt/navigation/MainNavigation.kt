package de.syntax_institut.androidabschlussprojekt.navigation

import android.util.Log // Import Log
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
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

@Composable
fun MainNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onNavigateToMovieDetail: (Movie) -> Unit
) {
    val didLogout by authViewModel.didLogout.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    // Centralized logout navigation
    LaunchedEffect(didLogout) {
        if (didLogout) {
            Log.d("MainNavigation", "Logout detected. Navigating to AUTH_FLOW.")
            navController.navigate(Routes.AUTH_FlOW) {
                popUpTo(Routes.HOME) { inclusive = false } // Keep HOME, clear everything else
                launchSingleTop = true
            }
            authViewModel.clearLogoutFlag() // Clear the flag after navigation
        }
    }

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
                    onProfileClick = { navController.navigate(Routes.AUTH_FlOW) }, // Direct to AUTH_FLOW
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
                navArgument("posterPath") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("overview") { type = NavType.StringType },
                navArgument("releaseDate") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val posterPath = backStackEntry.arguments?.getString("posterPath")?.decodeURLPath()
            val title = backStackEntry.arguments?.getString("title")?.decodeURLPath()
            val overview = backStackEntry.arguments?.getString("overview")?.decodeURLPath()
            val releaseDate = backStackEntry.arguments?.getString("releaseDate")?.decodeURLPath()

            MovieDetailScreen(
                navController = navController,
                posterPath = posterPath,
                title = title,
                overview = overview,
                releaseDate = releaseDate
            )
        }

        // This is the unified profile/authentication entry point
        composable(Routes.AUTH_FlOW) {
            MainScaffold(navController = navController) { innerPadding ->
                // Log for debugging the state change
                Log.d("MainNavigation", "AUTH_FLOW composable, isAuthenticated: $isAuthenticated")
                if (isAuthenticated) {
                    Log.d("MainNavigation", "Authenticated: Displaying ProfileScreen.")
                    ProfileScreen(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                    )
                } else {
                    Log.d("MainNavigation", "Not authenticated: Displaying AuthScreen.")
                    AuthScreen(
                        modifier = Modifier.padding(innerPadding),
                        onLoginSuccess = {
                            // This callback is triggered when AuthScreen's internal logic determines
                            // it should navigate away (e.g., after successful login/registration)
                            Log.d("MainNavigation", "AuthScreen onLoginSuccess called. Current isAuthenticated: $isAuthenticated")
                            // The isAuthenticated state will have already been updated by AuthViewModel.
                            // We don't need to re-navigate to AUTH_FLOW here because the AuthScreen
                            // is already being displayed *within* AUTH_FLOW.
                            // When isAuthenticated becomes true, the `if (isAuthenticated)` condition
                            // above will automatically cause a recomposition to show ProfileScreen.
                            // No explicit navController.navigate(Routes.AUTH_FLOW) needed here from onLoginSuccess
                            // unless you want to explicitly push a new instance or clear stack.
                            // For a seamless transition, simply letting the state change propagate is best.
                            // However, if you explicitly navigate to AUTH_FLOW *again*, it ensures
                            // the AuthScreen instance is popped and ProfileScreen is pushed cleanly.

                            // Let's keep the navigate for explicit stack management.
                            // This pops the current AuthScreen instance and pushes a new AUTH_FLOW
                            // which then immediately resolves to ProfileScreen.
                            navController.navigate(Routes.AUTH_FlOW) {
                                popUpTo(Routes.AUTH_FlOW) { inclusive = true } // Replace AuthScreen with ProfileScreen
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }

        // REMOVE THIS COMPOSABLE:
        // composable(Routes.PROFILE) {
        //     MainScaffold(navController = navController) { innerPadding ->
        //         ProfileScreen(
        //             modifier = Modifier.padding(innerPadding),
        //             navController = navController
        //         )
        //     }
        // }
    }
}