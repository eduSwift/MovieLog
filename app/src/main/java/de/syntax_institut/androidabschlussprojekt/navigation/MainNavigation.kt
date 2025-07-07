package de.syntax_institut.androidabschlussprojekt.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.ui.screens.AuthScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.ProfileScreen
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel

@Composable
fun MainNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToMovieDetail: (Movie) -> Unit
) {
    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(
                authViewModel = authViewModel,
                onLoginSuccess = onLoginSuccess
            )
        }
        composable("profile") {
            ProfileScreen(authViewModel = authViewModel)
        }

        // Optional: Add other destinations like movie detail here
        // e.g., composable("movieDetail/{...}") { ... }
    }
}
