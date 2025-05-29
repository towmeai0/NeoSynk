package com.ayudevices.neosynkparent.ui.screen.auth

import android.app.Activity
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ayudevices.neosynkparent.R
import com.ayudevices.neosynkparent.ui.screen.Screen
import com.ayudevices.neosynkparent.ui.theme.OrangeAccent
import com.ayudevices.neosynkparent.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.identity.Identity
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SignupScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navController: NavHostController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as Activity

    val googleAuthClient = remember { GoogleAuthUiClient(context) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val credential = Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            val name = credential.displayName
            val email = credential.id
            val profilePicUri = credential.profilePictureUri

            Log.d("GoogleSignIn", "ID Token: $idToken")
            Log.d("GoogleSignIn", "Name: $name")
            Log.d("GoogleSignIn", "Email: $email")
            Log.d("GoogleSignIn", "Profile Pic: $profilePicUri")

            idToken?.let {
                googleAuthClient.handleSignInResult(it) { success, error ->
                    if (success) {
                        // Pass the Google user's name to ProfileScreen
                        onGoogleSignupSuccess(navController, name ?: "")
                    } else {
                        Toast.makeText(context, error ?: "Google Sign-In failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 16.dp, vertical = 32.dp)
    ) {
        Text(
            text = "NeoSynk",
            color = Color(0xFFB7FABD),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StyledTextField(value = email, onValueChange = { email = it }, label = "Email")
            StyledTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)
            StyledTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password", isPassword = true)
        }

        Spacer(modifier = Modifier.height(30.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    errorMessage = "Please fill all fields"
                } else if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    errorMessage = "Invalid email format"
                } else {
                    isLoading = true
                    errorMessage = ""
                    viewModel.signUp(email, password, {
                        onSignupSuccess(navController)
                    }, {
                        errorMessage = it
                        isLoading = false
                    })
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Sign Up", color = Color.White, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        GoogleSignInButton {
            googleAuthClient.signIn(launcher)
        }
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google_logo),
            contentDescription = "Google Sign In",
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Continue with Google", color = Color.Black)
    }
}

fun onSignupSuccess(navController: NavHostController) {
    navController.navigate(Screen.Profile.route) {
        popUpTo(0)
    }
}

// Updated function for Google Sign-In success with better logging
fun onGoogleSignupSuccess(navController: NavHostController, userName: String) {
    Log.d("SignupScreen", "Navigating with Google user name: $userName")
    try {
        // URL encode the name to handle special characters and spaces
        val encodedName = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())
        Log.d("SignupScreen", "Encoded name: $encodedName")
        val route = "${Screen.Profile.route}?googleUserName=$encodedName"
        Log.d("SignupScreen", "Navigation route: $route")
        navController.navigate(route) {
            popUpTo(0)
        }
    } catch (e: Exception) {
        Log.e("SignupScreen", "Error in navigation", e)
        // Fallback navigation without parameters
        navController.navigate(Screen.Profile.route) {
            popUpTo(0)
        }
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(5.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFEC7344),
            unfocusedBorderColor = Color(0xFFEC7344),
            cursorColor = OrangeAccent,
            focusedLabelColor = Color.LightGray,
            unfocusedLabelColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}