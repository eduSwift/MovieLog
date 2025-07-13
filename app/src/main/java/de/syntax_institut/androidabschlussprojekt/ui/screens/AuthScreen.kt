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
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
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
    val showRegistrationSuccessMessage by authViewModel.showRegistrationSuccess.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    // ✅ Navigate on successful login
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onLoginSuccess()
        }
    }

    // ✅ Show success message after registration
    LaunchedEffect(wasJustRegistered) {
        if (wasJustRegistered) {
            Log.d("AuthScreen", "wasJustRegistered detected. Showing success message.")
            authViewModel.setShowRegistrationSuccess(true)
            isLoginMode = true
            email = ""
            password = ""
            repeatPassword = ""
            authViewModel.clearWasJustRegisteredFlag()
            authViewModel.setError(null)
        }
    }

    // ✅ Auto-hide the success message after 3 seconds
    LaunchedEffect(showRegistrationSuccessMessage) {
        if (showRegistrationSuccessMessage) {
            delay(3000)
            authViewModel.clearRegistrationSuccessMessage()
        }
    }

    // ✅ Clear error when switching modes
    LaunchedEffect(isLoginMode) {
        authViewModel.setError(null)
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

        // ✅ Show registration success message above the login button only in login mode
        if (isLoginMode && showRegistrationSuccessMessage) {
            Text(
                text = "Your account was created successfully! Please sign in.",
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                if (isLoginMode) {
                    authViewModel.login(email, password)
                } else {
                    if (password != repeatPassword) {
                        authViewModel.setError("Passwords do not match")
                        return@Button
                    }
                    authViewModel.register(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isLoginMode) "Login" else "Sign Up")
        }

        TextButton(
            onClick = {
                isLoginMode = !isLoginMode
                authViewModel.setError(null)
                email = ""
                password = ""
                repeatPassword = ""
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
    }
}
