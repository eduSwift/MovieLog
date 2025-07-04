package de.syntax_institut.androidabschlussprojekt.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.ui.components.MainScaffold
import de.syntax_institut.androidabschlussprojekt.ui.screens.HomeScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.MovieDetailScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.SearchScreen
import androidx.compose.ui.Modifier
import de.syntax_institut.androidabschlussprojekt.ui.screens.ProfileScreen


@Composable
fun MainNavigation(
    navController: NavHostController,
    onNavigateToMovieDetail: (Movie) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            MainScaffold(navController = navController) { innerPadding ->
                HomeScreen(
                    modifier = Modifier.padding(innerPadding),
                    onMovieClick = onNavigateToMovieDetail,
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                    onProfileClick = { navController.navigate(Routes.PROFILE) }
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

        composable(Routes.PROFILE) {
            MainScaffold(navController = navController) { innerPadding ->
                ProfileScreen()
            }
        }
    }
}