package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import de.syntax_institut.androidabschlussprojekt.navigation.Routes
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    isAuthenticated: Boolean,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUserId by authViewModel.currentUserId.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            navController.navigate(Routes.PROFILE_ENTRY) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFB3D7EA))
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("User ID: ${currentUserId ?: "Unknown"}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showLogoutDialog = true }) {
            Text("Logout")
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout Confirmation") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
