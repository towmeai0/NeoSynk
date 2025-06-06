package com.ayudevices.neosynkparent.ui.screen.auth

import android.util.Log
import android.util.Patterns
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ayudevices.neosynkparent.di.NetworkModule.TokenSenderEntryPoint
import com.ayudevices.neosynkparent.ui.screen.Screen
import com.ayudevices.neosynkparent.ui.theme.OrangeAccent
import com.ayudevices.neosynkparent.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val email = remember { mutableStateOf("") }
    val pass = remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Black)
    ) {
        Text(
            "NeoSynk",
            color = Color(0xFFB7FABD),
            fontSize = 75.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 100.dp, start = 27.dp, end = 27.dp, top = 27.dp)
        )

        Column(
            modifier = Modifier
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            StyledTextField(value = email.value, onValueChange = { email.value = it }, label = "Email id or Mobile Number")
            StyledTextField(value = pass.value, onValueChange = { pass.value = it }, label = "Password", isPassword = true)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Button(
            onClick = {
                if (email.value.isEmpty() || pass.value.isEmpty()) {
                    errorMessage = "Please fill all fields"
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
                    errorMessage = "Invalid email format"
                } else {
                    isLoading = true
                    errorMessage = ""
                    viewModel.signIn(
                        email.value,
                        pass.value,
                        {
                            isLoading = false
                            onLoginSuccess(navController)
                        },
                        { error ->
                            isLoading = false
                            errorMessage = error
                        }
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Log In", color = Color.White, fontSize = 16.sp)
            }
        }

    }
}

fun onLoginSuccess(navController: NavHostController) {
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM", "Token on login: $fcmToken")

                val context = navController.context.applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(
                    context,
                    TokenSenderEntryPoint::class.java
                )
                entryPoint.tokenSender().sendFcmTokenToServer(fcmToken)
            } else {
                Log.e("FCM", "Failed to get token: ", task.exception)
            }
        }

    navController.navigate(Screen.Home.route) {
        popUpTo(Screen.Home.route) { inclusive = true }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}
