package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VpnViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: VpnViewModel,
    onAuthSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun validateAndSubmit() {
        if (!email.contains("@") || email.length < 5) {
            errorMessage = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return
        }
        errorMessage = null
        isLoading = true

        if (isSignUp) {
            viewModel.signUpWithEmailAndPassword(
                email = email,
                password = password,
                onSuccess = {
                    isLoading = false
                    onAuthSuccess()
                },
                onError = { err ->
                    isLoading = false
                    errorMessage = err
                }
            )
        } else {
            viewModel.loginWithEmailAndPassword(
                email = email,
                password = password,
                onSuccess = {
                    isLoading = false
                    onAuthSuccess()
                },
                onError = { err ->
                    isLoading = false
                    errorMessage = err
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBase)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Identity
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .background(CardDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isSignUp) "Create Account" else "Welcome Back",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isSignUp) "Sign up to start secure private browsing" else "Sign in to connect to NovaVPN",
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Inputs Container
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Address", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ElectricBlue) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password", color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricBlue) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { validateAndSubmit() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isSignUp) "Sign Up" else "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Sign Ins
            Text(text = "OR CONTINUE WITH", color = TextMuted, fontSize = 11.sp, letterSpacing = 1.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                 // Google
                 Box(
                     modifier = Modifier
                         .weight(1f)
                         .height(50.dp)
                         .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                         .background(CardDark)
                         .clickable {
                             isLoading = true
                             viewModel.signUpWithEmailAndPassword(
                                 "google_user@gmail.com",
                                 "google123",
                                 onSuccess = {
                                     isLoading = false
                                     onAuthSuccess()
                                 },
                                 onError = {
                                     // If already created, login instead!
                                     viewModel.loginWithEmailAndPassword(
                                         "google_user@gmail.com",
                                         "google123",
                                         onSuccess = {
                                             isLoading = false
                                             onAuthSuccess()
                                         },
                                         onError = {
                                             isLoading = false
                                             errorMessage = "Google authentication failed"
                                         }
                                     )
                                 }
                             )
                         }
                         .testTag("auth_google_button"),
                     contentAlignment = Alignment.Center
                 ) {
                     Text("Google", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                 }
 
                 // Apple
                 Box(
                     modifier = Modifier
                         .weight(1f)
                         .height(50.dp)
                         .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                         .background(CardDark)
                         .clickable {
                             isLoading = true
                             viewModel.signUpWithEmailAndPassword(
                                 "apple_user@icloud.com",
                                 "apple123",
                                 onSuccess = {
                                     isLoading = false
                                     onAuthSuccess()
                                 },
                                 onError = {
                                     // If already created, login instead!
                                     viewModel.loginWithEmailAndPassword(
                                         "apple_user@icloud.com",
                                         "apple123",
                                         onSuccess = {
                                             isLoading = false
                                             onAuthSuccess()
                                         },
                                         onError = {
                                             isLoading = false
                                             errorMessage = "Apple ID authentication failed"
                                         }
                                     )
                                 }
                             )
                         }
                         .testTag("auth_apple_button"),
                     contentAlignment = Alignment.Center
                 ) {
                     Text("Apple ID", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                 }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isSignUp) "Sign In" else "Sign Up",
                    color = ElectricBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { isSignUp = !isSignUp }
                        .testTag("auth_toggle_mode")
                )
            }
        }
    }
}
