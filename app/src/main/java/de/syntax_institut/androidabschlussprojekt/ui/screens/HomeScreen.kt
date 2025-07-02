package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory
import de.syntax_institut.androidabschlussprojekt.ui.components.AppLogoHeader
import de.syntax_institut.androidabschlussprojekt.ui.components.MovieList
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.HomeScreenViewModel
import org.koin.androidx.compose.koinViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun String.decodeURLPath(): String {
    return URLDecoder.decode(this, StandardCharsets.UTF_8.toString())
}

fun String.encodeURLPath(): String {
    return URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
        .replace("+", "%20")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val moviesList by viewModel.movies.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val navigateToMovie by viewModel.navigateToMovieDetail.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFFB3D7EA)
    val navController = rememberNavController()

    val onMovieClick: (Movie) -> Unit = { movie ->
        viewModel.setMovieToNavigateTo(movie)
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
            AppLogoHeader()
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
                    onClick = {
                        selectedTab = 1
                        navController.navigate("search_screen") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Person") },
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true }
                                    .background(Color.White)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Category: ${selectedCategory.name.replace("_", " ")}",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select category")
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.5f)
                            ) {
                                MovieCategory.entries.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name.replace("_", " ")) },
                                        onClick = {
                                            viewModel.updateSelectedCategory(category)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        MovieList(movies = moviesList, onMovieClick = onMovieClick)
                    }
                }

                composable("search_screen") {
                    SearchScreen(onMovieClick = onMovieClick)
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