package com.ayudevices.neosynkparent.ui.screen.auth

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
import com.ayudevices.neosynkparent.ui.screen.Screen
import com.ayudevices.neosynkparent.ui.theme.OrangeAccent
import com.ayudevices.neosynkparent.ui.theme.AppBackground
import com.ayudevices.neosynkparent.ui.theme.CardBackground
import com.ayudevices.neosynkparent.ui.theme.neoSynkLightGreen

@Composable
fun SplashScreen(
    navController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top Space or Status Bar Placeholder
            Spacer(modifier = Modifier.height(40.dp))

            // Main Welcome Texts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome to,",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "NeoSynk",
                    color =  neoSynkLightGreen,
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

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Info Container
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
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ditch newborn guesswork with NeoSynk — your pocket guru tracking heart, growth, sleep and feeding for delightful details!",
                        color = Color.Black,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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
