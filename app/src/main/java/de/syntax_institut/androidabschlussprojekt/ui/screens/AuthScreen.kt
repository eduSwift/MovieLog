package de.syntax_institut.androidabschlussprojekt.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var isLoginMode by rememberSaveable { mutableStateOf(true) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var accountCreatedMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val justSignedUp by authViewModel.justSignedUp.collectAsState()

    Log.d("AuthScreen", "Recomposed. justSignedUp: $justSignedUp, accountCreatedMessage: $accountCreatedMessage")

    LaunchedEffect(Unit) {
        if (email.isBlank() && password.isBlank() && repeatPassword.isBlank()) {
            errorMessage = null
        }
        isLoading = false
    }

    // This LaunchedEffect specifically handles the 'just signed up' event
    LaunchedEffect(justSignedUp) {
        if (justSignedUp) {
            accountCreatedMessage = "Account successfully created!"
            isLoginMode = true // Switch to login mode to show the message on the login form
            email = "" // Clear password related fields for security and fresh input
            password = ""
            repeatPassword = ""
            // IMPORTANT: Clear the ViewModel flag IMMEDIATELY after observing it
            authViewModel.clearSignUpFlag()
            Log.d("AuthScreen", "Detected justSignedUp. Message set to: '$accountCreatedMessage'. ViewModel flag cleared.")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isLoginMode) "Login" else "Sign Up",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
        )

        if (!isLoginMode) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = repeatPassword,
                onValueChange = { repeatPassword = it },
                label = { Text("Repeat Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Repeat Password Icon") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                errorMessage = null

                if (isLoginMode) {
                    // Only clear the account created message when attempting a LOGIN
                    accountCreatedMessage = null
                    authViewModel.signIn(
                        email = email,
                        password = password,
                        onSuccess = {
                            isLoading = false
                            accountCreatedMessage = null // Ensure cleared on successful login
                            onLoginSuccess() // Navigate away on success
                        },
                        onError = {
                            isLoading = false
                            errorMessage = it
                        }
                    )
                } else { // This is the Sign Up path
                    if (password != repeatPassword) {
                        isLoading = false
                        errorMessage = "Passwords do not match"
                        return@Button
                    }

                    // DO NOT clear accountCreatedMessage = null here for the signup path.
                    // It should be set by the LaunchedEffect after navigation.
                    authViewModel.signUp(
                        email = email,
                        password = password,
                        onSuccess = {
                            isLoading = false
                            // The message will be set by the LaunchedEffect(justSignedUp) when it runs
                            onSignUpSuccess() // Navigates back to AuthScreen
                        },
                        onError = {
                            isLoading = false
                            errorMessage = it
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank() && password.isNotBlank() && (!isLoading)
        ) {
            Text(if (isLoginMode) "Login" else "Sign Up")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                isLoginMode = !isLoginMode
                errorMessage = null
                // IMPORTANT: Clear the message when switching modes
                accountCreatedMessage = null
                repeatPassword = ""
                // Also clear the ViewModel flag if user explicitly switches mode
                authViewModel.clearSignUpFlag()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (isLoginMode) "Don't have an account yet? Sign up" else "Already have an account? Log in")
        }

        // Display error message
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                modifier = Modifier.padding(top = 12.dp)
            )
            Log.d("AuthScreen", "Error message displayed: '$errorMessage'")
        }

        // Display account created message - this should be below the buttons
        if (accountCreatedMessage != null) {
            Text(
                text = accountCreatedMessage ?: "",
                color = Color(0xFF2E7D32), // A nice green color
                style = MaterialTheme.typography.bodyLarge, // Adjust style if needed
                modifier = Modifier.padding(top = 12.dp)
            )
            Log.d("AuthScreen", "Account created message DISPLAYED: '$accountCreatedMessage'")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
        }
    }
}