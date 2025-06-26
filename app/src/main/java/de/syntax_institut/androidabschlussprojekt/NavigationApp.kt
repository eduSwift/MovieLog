package de.syntax_institut.androidabschlussprojekt


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.ui.components.MovieList
import de.syntax_institut.androidabschlussprojekt.ui.screens.HomeScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.MovieDetailScreen
import de.syntax_institut.androidabschlussprojekt.ui.screens.decodeURLPath
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.HomeScreenViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


fun String.encodeURLPath(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
        .replace("+", "%20")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationApp(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel()
) {
    val navController = rememberNavController()
    val moviesList by viewModel.movies.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val navigateToMovie by viewModel.navigateToMovieDetail.collectAsState()

    var active by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val backgroundColor = Color(0xFFB3D7EA)

    val onMovieClick: (Movie) -> Unit = { movie ->
        viewModel.setMovieToNavigateTo(movie)
        active = 0
    }

    LaunchedEffect(navigateToMovie) {
        navigateToMovie?.let { movie ->
            val encodedTitle = movie.title.encodeURLPath()
            val encodedOverview = movie.overview.encodeURLPath()
            val encodedPosterPath = movie.poster_path?.encodeURLPath() ?: ""
            val encodedReleaseDate = movie.release_date.encodeURLPath()

            navController.navigate(
                "movie_detail_route/$encodedPosterPath/$encodedTitle/$encodedOverview/$encodedReleaseDate"
            )
            viewModel.onNavigationCompleted()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onSearch = { active = 0 },
                active = (active == 1),
                onActiveChange = { active = if(it) 1 else 0 },
                placeholder = { Text("Search movies...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(16.dp)
                ) {
                    MovieList(movies = moviesList, onMovieClick = onMovieClick)
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate("home_main_content") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Discover") },
                    label = { Text("Discover") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            NavHost(
                navController = navController,
                startDestination = "home_main_content",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable("home_main_content") {
                    HomeScreen(
                        viewModel = viewModel,
                        onMovieClick = onMovieClick
                    )
                }

                composable(
                    route = "movie_detail_route/{posterPath}/{title}/{overview}/{releaseDate}",
                    arguments = listOf(
                        navArgument("posterPath") { type = androidx.navigation.NavType.StringType; nullable = true },
                        navArgument("title") { type = androidx.navigation.NavType.StringType; nullable = true },
                        navArgument("overview") { type = androidx.navigation.NavType.StringType; nullable = true },
                        navArgument("releaseDate") { type = androidx.navigation.NavType.StringType; nullable = true }
                    )
                ) { backStackEntry ->
                    val posterPath = backStackEntry.arguments?.getString("posterPath")
                    val title = backStackEntry.arguments?.getString("title")
                    val overview = backStackEntry.arguments?.getString("overview")
                    val releaseDate = backStackEntry.arguments?.getString("releaseDate")
                    MovieDetailScreen(
                        navController = navController,
                        posterPath = posterPath?.decodeURLPath(),
                        title = title?.decodeURLPath(),
                        overview = overview?.decodeURLPath(),
                        releaseDate = releaseDate?.decodeURLPath()
                    )
                }
            }
        }
    }
}