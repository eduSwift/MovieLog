package de.syntax_institut.androidabschlussprojekt.ui.components

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
    content: @Composable (PaddingValues) -> Unit
) {
    val backgroundColor = Color(0xFFB3D7EA)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Routes.HOME to Icons.Default.Home,
        Routes.SEARCH to Icons.Default.Search,
        Routes.PROFILE to Icons.Default.Person
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = backgroundColor) {
                items.forEachIndexed { index, (route, icon) ->
                    NavigationBarItem(
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(Routes.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = route) },
                        label = { Text(route.replaceFirstChar { it.uppercaseChar() }) }
                    )
                }
            }
        },
        containerColor = backgroundColor,
        content = content
    )
}