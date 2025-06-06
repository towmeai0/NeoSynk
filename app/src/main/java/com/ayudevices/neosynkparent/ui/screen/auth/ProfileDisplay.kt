package com.ayudevices.neosynkparent.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ayudevices.neosynkparent.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileDisplay(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val name by viewModel.name
    val loc by viewModel.location
    val gender by viewModel.gender
    val loading by viewModel.loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (loading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    ProfileTextItem(label = "Name", value = name)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileTextItem(label = "Location", value = loc)
                    Spacer(modifier = Modifier.height(8.dp))
                    ProfileTextItem(label = "Gender", value = gender)
                }
            }

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("Login") {
                        popUpTo(0)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            ) {
                Text(text = "Logout", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ProfileTextItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = if (value.isNotEmpty()) value else "N/A", color = Color.White, fontSize = 16.sp)
    }
}
