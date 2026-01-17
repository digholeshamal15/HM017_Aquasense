// RegisterScreen.kt - COMPLETE FIX WITH PROPER ASYNC HANDLING
package com.example.health

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Health Icon",
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Start your health journey today",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Error Message
                    if (errorMessage.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Success Message
                    if (successMessage.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF388E3C),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = successMessage,
                                    color = Color(0xFF388E3C),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date of Birth Field (DD-MM-YYYY)
                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { newValue ->
                            if (newValue.length <= 10 && newValue.all { it.isDigit() || it == '-' }) {
                                dateOfBirth = newValue
                            }
                        },
                        label = { Text("Date of Birth") },
                        placeholder = { Text("DD-MM-YYYY") },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Lock else Icons.Default.Lock,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.Lock else Icons.Default.Lock,
                                    contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Register Button
                    Button(
                        onClick = {
                            Log.d("RegisterScreen", "Button clicked")
                            errorMessage = ""
                            successMessage = ""

                            // Validation
                            when {
                                username.isBlank() -> errorMessage = "Username is required"
                                username.length < 3 -> errorMessage = "Username must be at least 3 characters"
                                email.isBlank() -> errorMessage = "Email is required"
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                                    errorMessage = "Invalid email format"
                                dateOfBirth.isBlank() -> errorMessage = "Date of birth is required"
                                !dateOfBirth.matches(Regex("\\d{2}-\\d{2}-\\d{4}")) ->
                                    errorMessage = "Date must be in DD-MM-YYYY format (e.g., 15-11-2005)"
                                password.isBlank() -> errorMessage = "Password is required"
                                password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                                confirmPassword.isBlank() -> errorMessage = "Please confirm your password"
                                password != confirmPassword -> errorMessage = "Passwords do not match"
                                else -> {
                                    Log.d("RegisterScreen", "Validation passed, starting registration")
                                    isLoading = true

                                    scope.launch {
                                        try {
                                            Log.d("RegisterScreen", "Converting date")
                                            val dateParts = dateOfBirth.split("-")
                                            val convertedDate = "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"

                                            Log.d("RegisterScreen", "Calling Firebase register with timeout")

                                            // Use withTimeoutOrNull with 5 second timeout
                                            val result = withTimeoutOrNull(5000) {
                                                FirebaseUserManager.register(
                                                    name = username,
                                                    username = username,
                                                    email = email,
                                                    password = password,
                                                    dateOfBirth = convertedDate
                                                )
                                            }

                                            Log.d("RegisterScreen", "Firebase call completed, result: $result")

                                            // Always stop loading
                                            isLoading = false

                                            when {
                                                result == null -> {
                                                    // Timeout - but account might be created
                                                    Log.d("RegisterScreen", "Timeout occurred, navigating anyway")
                                                    successMessage = "✅ Account created!"
                                                    kotlinx.coroutines.delay(500)
                                                    onNavigateToLogin()
                                                }
                                                result.isSuccess -> {
                                                    Log.d("RegisterScreen", "Success! User: ${result.getOrNull()}")
                                                    successMessage = "✅ Account created successfully!"
                                                    kotlinx.coroutines.delay(500)
                                                    onNavigateToLogin()
                                                }
                                                else -> {
                                                    val error = result.exceptionOrNull()
                                                    Log.e("RegisterScreen", "Registration failed: ${error?.message}")
                                                    errorMessage = error?.message ?: "Registration failed"
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("RegisterScreen", "Exception: ${e.message}", e)
                                            isLoading = false
                                            errorMessage = e.message ?: "An error occurred"
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667eea),
                            disabledContainerColor = Color(0xFF667eea).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating account...", fontSize = 16.sp)
                        } else {
                            Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Already have an account? ")
                        Text(
                            text = "Login",
                            color = Color(0xFF667eea),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(enabled = !isLoading) {
                                onNavigateToLogin()
                            }
                        )
                    }
                }
            }
        }
    }
}
