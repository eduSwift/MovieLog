package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel


@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val userId by authViewModel.currentUserId.collectAsState()
    val wasJustRegistered by authViewModel.wasJustRegistered.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val showSignedInScreen = isAuthenticated && !wasJustRegistered

    LaunchedEffect(isAuthenticated, wasJustRegistered) {
        if (isAuthenticated && !wasJustRegistered) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB3D7EA))
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (!showSignedInScreen) {
            Text(
                text = if (isLoginMode) "Sign In" else "Sign Up",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (!isLoginMode) {
                OutlinedTextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    label = { Text("Repeat Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isLoginMode) {
                        authViewModel.login(email, password)
                        showSuccessMessage = false
                    } else {
                        if (password != repeatPassword) {
                            authViewModel.setError("Passwords do not match")
                            return@Button
                        }
                        authViewModel.register(email, password)
                        showSuccessMessage = true
                        email = ""
                        password = ""
                        repeatPassword = ""
                        isLoginMode = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isLoginMode) "Login" else "Sign Up")
            }

            TextButton(
                onClick = {
                    isLoginMode = !isLoginMode
                    showSuccessMessage = false
                    authViewModel.setError(null)
                }
            ) {
                Text(
                    text = if (isLoginMode) "Need an account? Sign up" else "Already have an account? Log in"
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            if (showSuccessMessage && wasJustRegistered) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your account was created successfully! Please sign in.")
            }

        } else {
            Text(text = "You're signed in!", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            userId?.let {
                Text(text = "User ID: $it", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { authViewModel.logout() }) {
                Text("Logout")
            }
        }
    }
}
