package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldPrimary

@Composable
fun AuthScreen(
    onLoginSuccess: (email: String, username: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRegister by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lulugram",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRegister) "Create your account" else "Sign in to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isRegister) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank() || (isRegister && username.isBlank())) {
                            errorMessage = "Please fill in all fields"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        if (isRegister) {
                            auth.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnSuccessListener { result ->
                                    val uid = result.user?.uid ?: ""
                                    val user = com.example.data.model.User(
                                        id = uid,
                                        username = username.trim().lowercase(),
                                        displayName = username.trim(),
                                        email = email.trim(),
                                        createdAt = System.currentTimeMillis()
                                    )
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        .collection("users").document(uid)
                                        .set(com.example.data.remote.FirestoreMappers.userToMap(user))
                                        .addOnCompleteListener {
                                            isLoading = false
                                            onLoginSuccess(email, username)
                                        }
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    errorMessage = it.localizedMessage ?: "Registration failed"
                                }
                        } else {
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnSuccessListener { result ->
                                    isLoading = false
                                    onLoginSuccess(email, "")
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    errorMessage = it.localizedMessage ?: "Sign in failed"
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color(0xFF1A1100)
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF1A1100),
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isRegister) "Sign Up" else "Log In")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isRegister = !isRegister }) {
                    Text(
                        text = if (isRegister) "Already have an account? Log In" else "Don't have an account? Sign Up",
                        color = GoldPrimary
                    )
                }
            }
        }
    }
}
