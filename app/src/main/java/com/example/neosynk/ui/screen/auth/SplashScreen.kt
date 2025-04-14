package com.example.neosynk.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.neosynk.ui.screen.Screen
import com.example.neosynk.ui.theme.OrangeAccent
import com.example.neosynk.ui.theme.AppBackground
import com.example.neosynk.ui.theme.CardBackground

@Composable
fun SplashScreen(
    navController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Status Bar placeholder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add status indicators here if needed
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Column {
                Text(
                    text = "Welcome to,",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "NeoSynk",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 70.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Nurturing Your\nNewborn, Every Beat",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 28.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Push bottom container to bottom

            // Bottom Rounded Info + CTA Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        color = CardBackground,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ditch newborn guesswork with NeoSynk — your pocket guru tracking heart, growth, sleep and feeding for delightful details!",
                        color = Color.Black,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
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
                    ) {
                        Text(text = "Get Started", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
