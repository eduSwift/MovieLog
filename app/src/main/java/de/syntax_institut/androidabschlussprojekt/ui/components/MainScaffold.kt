package de.syntax_institut.androidabschlusprojekt.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import de.syntax_institut.androidabschlussprojekt.navigation.Routes


@Composable
fun MainScaffold(
    navController: NavController,
    isAuthenticated: Boolean,
    content: @Composable (PaddingValues) -> Unit
) {
    val backgroundColor = Color(0xFFB3D7EA)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Pair(Routes.HOME, Icons.Default.Home),
        Pair(Routes.SEARCH, Icons.Default.Search),
        Pair(Routes.AUTH, Icons.Default.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = backgroundColor) {
                items.forEach { (route, icon) ->
                    val selectedRoute = if (route == Routes.AUTH && isAuthenticated) Routes.PROFILE else route

                    NavigationBarItem(
                        selected = currentRoute == selectedRoute || (currentRoute == Routes.PROFILE && route == Routes.AUTH),
                        onClick = {
                            val targetRoute = if (route == Routes.AUTH) {
                                if (isAuthenticated) Routes.PROFILE else Routes.AUTH
                            } else {
                                route
                            }

                            navController.navigate(targetRoute) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                    inclusive = false
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = route) },
                        label = {
                            Text(
                                when (route) {
                                    Routes.AUTH -> "Profile"
                                    Routes.HOME -> "Home"
                                    Routes.SEARCH -> "Search"
                                    else -> route.replaceFirstChar { it.uppercaseChar() }
                                }
                            )
                        }
                    )
                }
            }
        },
        containerColor = backgroundColor,
        content = content
    )
}