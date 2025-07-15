package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavHostController,
    isDarkModeEnabled: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    settingsViewModel: SettingsViewModel,
    onEditNickname: () -> Unit,
    onChangeProfilePicture: () -> Unit,
    onChangePassword: () -> Unit,
    onDeleteAccount: () -> Unit,
    onLogout: () -> Unit
) {
    val isDarkModeEnabled by settingsViewModel.isDarkModeEnabled.collectAsState()
    val scrollState = rememberScrollState()

    // Apply full background color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB3D7EA)) // consistent background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            SettingsOptionRow(
                icon = Icons.Default.Person,
                title = "Edit Nickname",
                onClick = onEditNickname
            )

            SettingsOptionRow(
                icon = Icons.Default.Image,
                title = "Change Profile Picture",
                onClick = onChangeProfilePicture
            )

            SettingsOptionRow(
                icon = Icons.Default.Lock,
                title = "Change Password",
                onClick = onChangePassword
            )

            SettingsOptionRow(
                icon = Icons.Default.Delete,
                title = "Delete Account",
                onClick = onDeleteAccount
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Dark Mode"
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Dark Mode",
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp
                )
                Switch(
                    checked = isDarkModeEnabled,
                    onCheckedChange = { checked ->
                        settingsViewModel.setDarkModeEnabled(checked)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
fun SettingsOptionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp)
    }
}