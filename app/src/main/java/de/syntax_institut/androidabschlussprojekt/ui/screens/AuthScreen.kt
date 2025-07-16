package de.syntax_institut.androidabschlussprojekt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onLoginSuccess: () -> Unit // Callback for successful login
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) } // Start in login mode
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var accountCreatedMessage by remember { mutableStateOf<String?>(null) }

    val justSignedUp by authViewModel.justSignedUp.collectAsState()

    // Reset state when screen is shown (e.g., after logout or deletion)
    LaunchedEffect(Unit) {
        email = ""
        password = ""
        repeatPassword = ""
        errorMessage = null
        // isLoginMode is controlled by the justSignedUp flag below, or defaults to true
        accountCreatedMessage = null
        isLoading = false
    }

    // Observe justSignedUp flag from ViewModel
    LaunchedEffect(justSignedUp) {
        if (justSignedUp) {
            accountCreatedMessage = "Account successfully created!"
            isLoginMode = true // Switch to login mode
            email = "" // Clear fields for new login
            password = ""
            repeatPassword = ""
            authViewModel.clearSignUpFlag() // Clear the flag after displaying the message
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
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
                accountCreatedMessage = null // Clear previous messages

                if (isLoginMode) {
                    authViewModel.signIn(
                        email = email,
                        password = password,
                        onSuccess = {
                            isLoading = false
                            onLoginSuccess() // Navigate to ProfileScreen
                        },
                        onError = {
                            isLoading = false
                            errorMessage = it
                        }
                    )
                } else {
                    if (password != repeatPassword) {
                        isLoading = false
                        errorMessage = "Passwords do not match"
                        return@Button
                    }

                    authViewModel.signUp(
                        email = email,
                        password = password,
                        onSuccess = {
                            isLoading = false
                            // After sign up, the LaunchedEffect(justSignedUp) will trigger
                            // to display the message and switch to login mode.
                            // No explicit navigation here for signup success.
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
                accountCreatedMessage = null // Clear message when switching modes
                repeatPassword = ""
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (isLoginMode) "Don't have an account yet? Sign up" else "Already have an account? Log in")
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // Display account created message
        if (accountCreatedMessage != null) {
            Text(
                text = accountCreatedMessage ?: "",
                color = Color(0xFF2E7D32), // A nice green color
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}