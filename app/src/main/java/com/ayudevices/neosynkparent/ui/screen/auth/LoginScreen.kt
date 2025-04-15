package com.ayudevices.neosynkparent.ui.screen.auth


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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ayudevices.neosynkparent.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val name = viewModel.name
    val number = viewModel.number
    val location = viewModel.location

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(20.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Parents Log In",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Picture Placeholder
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

        val borderColor = Color(0xFFFF9800)

        CustomOutlinedField(
            value = name,
            onValueChange = { viewModel.name = it },
            placeholder = "Name",
            borderColor = borderColor
        )

        CustomOutlinedField(
            value = number,
            onValueChange = { viewModel.number = it },
            placeholder = "Number",
            keyboardType = KeyboardType.Phone,
            borderColor = borderColor
        )

        CustomOutlinedField(
            value = location,
            onValueChange = { viewModel.location = it },
            placeholder = "Location",
            borderColor = borderColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        OrangeButton("Continue >", onClick = {
            navController.navigate("KidsLoginScreen")
        })

        Spacer(modifier = Modifier.height(10.dp))

        OrangeButton("Sign In", onClick = {
            // TODO: Add logic
        })

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedAuthButton(text = "Sign in with Google", icon = "G")
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedAuthButton(text = "Sign in with Apple ID", icon = "")
    }
}

@Composable
fun CustomOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    borderColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.LightGray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = textFieldColors(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun OrangeButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun OutlinedAuthButton(text: String, icon: String) {
    OutlinedButton(
        onClick = { /* Placeholder */ },
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFFF9800)),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(icon, color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
        Text(text, color = Color.White)
    }
}

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

@Preview(showBackground = true)
@Composable
fun MyComposablePreview() {
    LoginScreen(navController = rememberNavController())
}
