package com.example.neosynk.ui.screen.auth


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.navigation.NavController

@Composable
fun LoginScreen(
    navController: NavController
) {
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {


        Spacer(modifier = Modifier.height(24.dp))

        // Top Title
        Text(
            text = "Parents Log In",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Picture Area
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Upload Profile Picture",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Input Fields
        val borderColor = Color(0xFFFF9800)
        val textFieldModifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Name", color = Color.LightGray) },
            colors = textFieldColors(),
            modifier = textFieldModifier,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            placeholder = { Text("Number", color = Color.LightGray) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
            colors = textFieldColors(),
            modifier = textFieldModifier,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            placeholder = { Text("Location", color = Color.LightGray) },
            colors = textFieldColors(),
            modifier = textFieldModifier,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Continue Button
        Button(
            onClick = {
                navController.navigate("KidsLoginScreen")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Continue >", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sign In Button
        Button(
            onClick = { /* Handle Sign In */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Sign In", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sign in with Google
        OutlinedButton(
            onClick = { /* Google Sign In */ },
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFFF9800)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("G", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
            Text("Sign in with Google", color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sign in with Apple ID
        OutlinedButton(
            onClick = { /* Apple Sign In */ },
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFFF9800)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
            Text("Sign in with Apple ID", color = Color.White)
        }
    }
}

// Helper to remove all outlines and apply dark background styles
@Composable
fun textFieldColors() = TextFieldDefaults.colors(
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    cursorColor = Color.White,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
