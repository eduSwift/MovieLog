package de.syntax_institut.androidabschlussprojekt.navigation


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
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

    LaunchedEffect(didLogout) {
        if (didLogout) {
            navController.navigate(Routes.PROFILE_ENTRY) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
            authViewModel.clearLogoutFlag()
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
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                    onProfileClick = { navController.navigate(Routes.PROFILE_ENTRY) }
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

        composable(Routes.PROFILE_ENTRY) {
            LaunchedEffect(isAuthenticated) {
                val target = if (isAuthenticated) Routes.PROFILE else Routes.AUTH
                navController.navigate(target) {
                    popUpTo(Routes.HOME) { inclusive = false }
                    launchSingleTop = true
                }
            }

            Box {
                Text("Redirecting...")
            }
        }

        composable(Routes.AUTH) {
            MainScaffold(navController = navController) { innerPadding ->
                AuthScreen(
                    modifier = Modifier.padding(innerPadding),
                    onLoginSuccess = {
                        navController.popBackStack()
                        navController.navigate(Routes.PROFILE_ENTRY)
                    }
                )
            }
        }

        composable(Routes.PROFILE) {
            MainScaffold(navController = navController) { innerPadding ->
                ProfileScreen(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    isAuthenticated = isAuthenticated
                )
            }
        }
    }
}
