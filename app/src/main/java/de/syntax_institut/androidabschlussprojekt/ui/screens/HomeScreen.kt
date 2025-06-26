package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost // New import
import androidx.navigation.compose.composable // New import
import androidx.navigation.compose.rememberNavController // New import
import androidx.navigation.navArgument // New import for navigation arguments
import de.syntax_institut.androidabschlussprojekt.ui.components.MovieList
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.HomeScreenViewModel
import java.net.URLDecoder // For URL decoding
import java.nio.charset.StandardCharsets // For UTF-8 charset

// Helper extension function to safely decode URL parameters
fun String.decodeURLPath(): String {
    return URLDecoder.decode(this, StandardCharsets.UTF_8.toString())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel()
) {
    val moviesList by viewModel.movies.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val backgroundColor = Color(0xFFB3D7EA)
    val navController = rememberNavController() // Create the NavController

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { active = false  },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search movies...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Search results will appear here...")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        // Navigate to the main content route when Home tab is selected
                        navController.navigate("home_main_content") {
                            // Pop up to the start destination to avoid building a large back stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when re-selecting the same item
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
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
                        // You could navigate to a separate discover route here if you had one
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Discover") },
                    label = { Text("Discover") }
                )
            }
        }
    ) { innerPadding ->
        // NavHost defines your navigation graph and handles screen switching
        NavHost(
            navController = navController,
            startDestination = "home_main_content", // The initial screen when the app starts
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply padding from Scaffold
                .background(backgroundColor) // Apply the background color to the entire NavHost area
        ) {
            // Define the route for your main content (Home/Discover tabs)
            composable("home_main_content") {
                // The Box is no longer strictly needed if it only wraps the 'when' statement
                // but can be kept if you intend to add more elements outside the tabs logic later.
                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        0 -> MovieList(movies = moviesList, navController = navController) // Pass navController
                        1 -> Text("Discover Content (Future Tab)", modifier = Modifier.padding(16.dp)) // Added padding for discover text
                    }
                }
            }

            // Define the route for the Movie Detail Screen
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
                    posterPath = posterPath?.decodeURLPath(), // Decode URL parameters
                    title = title?.decodeURLPath(),
                    overview = overview?.decodeURLPath(),
                    releaseDate = releaseDate?.decodeURLPath()
                )
            }
        }
    }
}