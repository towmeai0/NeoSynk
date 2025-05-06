package com.ayudevices.neosynkparent.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ayudevices.neosynkparent.ui.screen.Screen
import com.ayudevices.neosynkparent.ui.theme.CardBackground
import com.ayudevices.neosynkparent.ui.theme.OrangeAccent
import com.google.firebase.auth.FirebaseAuth

@Composable
fun OnboardingScreen(
    navController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp)
        ) {
            Box(
                modifier = Modifier.height(320.dp)
                    .width(363.dp)
                    .padding(start = 20.dp, end = 20.dp)
            ){
                Text(
                    buildAnnotatedString {
                        append("Say hello to your\n")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("little one")
                        }
                        append(", and\ngoodbye to\n")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("sleepless nights\nof worry.")
                        }
                    },
                    color = Color(0xFFCFD8DC),
                    fontSize = 48.sp,
                    lineHeight = 64.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = CardBackground,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp, start = 4.dp, end = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ditch newborn guesswork with\nNeoSynk your pocket guru\ntracking heart, growth, sleep\nand feeding for delightful\ndetails!",
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Log-in Button (Outlined style)
                    Button(
                        onClick = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.SplashScreen.route) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(bottom = 10.dp , start = 10.dp , end = 10.dp)
                    ) {
                        Text(text = "Log In", color = Color.White, fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            navController.navigate(Screen.Signup.route) {
                                popUpTo(Screen.Signup.route) { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(bottom = 10.dp , start = 10.dp , end = 10.dp)
                    ) {
                        Text(text = "Sign Up", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MyComposablePreview() {
    OnboardingScreen(navController = rememberNavController())
}
