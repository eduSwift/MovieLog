package de.syntax_institut.androidabschlussprojekt.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val wasJustRegistered by authViewModel.wasJustRegistered.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            Log.d("AuthScreen", "isAuthenticated is now true. Navigating away.")
            onLoginSuccess() // Signal to MainNavigation to navigate to ProfileScreen
        }
    }

    // Reset error message when switching modes
    LaunchedEffect(isLoginMode) {
        authViewModel.setError(null)
        showSuccessMessage = false // Clear success message on mode switch
    }

    // When wasJustRegistered changes, if it's true, show the success message
    LaunchedEffect(wasJustRegistered) {
        if (wasJustRegistered) {
            showSuccessMessage = true
            // Important: Clear the flag after processing, so it doesn't trigger repeatedly
            authViewModel.clearWasJustRegisteredFlag() // You'll need to add this to AuthViewModel
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFB3D7EA))
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
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
                text = "Your account was created successfully! Please sign in."
            )
        }
    }
}
