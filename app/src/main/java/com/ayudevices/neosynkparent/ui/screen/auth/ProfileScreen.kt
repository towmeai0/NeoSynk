package com.ayudevices.neosynkparent.ui.screen.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ayudevices.neosynkparent.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    navController: NavController,
    googleUserName: String? = null
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var shouldNavigate by remember { mutableStateOf(false) }

    // Local state to track the UI values
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var email = FirebaseAuth.getInstance().currentUser?.email
    Log.d("FIrebase Email","EMAIL ID $email")

    // Set Google user name when available
    LaunchedEffect(googleUserName) {
        Log.d("ProfileScreen", "Received googleUserName: $googleUserName")
        if (!googleUserName.isNullOrEmpty() && googleUserName != "null") {
            try {
                val decodedName = URLDecoder.decode(googleUserName, StandardCharsets.UTF_8.toString())
                Log.d("ProfileScreen", "Decoded name: $decodedName")
                name = decodedName // Set local state directly
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error decoding name", e)
                name = googleUserName // Use original string if decoding fails
            }
        }
    }

    // Handle navigation after loading
    if (shouldNavigate) {
        LaunchedEffect(Unit) {
            delay(1000)
            isLoading = false
            shouldNavigate = false
            navController.navigate("home")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Profile",
                fontSize = 20.sp,
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Upload Profile Picture",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields using local state
            ParentProfileInputField(
                hint = "Name",
                value = name,
                onValueChange = {
                    name = it
                    Log.d("ProfileScreen", "Name changed to: $it")
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ParentProfileInputField(
                hint = "Location",
                value = location,
                onValueChange = { location = it }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Gender Radio Button Section
            GenderSelectionSection(
                selectedGender = gender,
                onGenderSelected = { gender = it }
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isEmpty() || location.isEmpty() || gender.isEmpty()) {
                        errorMessage = "Please fill in all fields."
                    } else {
                        errorMessage = ""
                        isLoading = true
                        // Update ViewModel with current values before saving
                        Log.d("EMailk ","Emailk $email")
                        viewModel.updateProfile(email, name, location, gender)
                        viewModel.saveUserProfile()
                        shouldNavigate = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Continue >", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ParentProfileInputField(hint: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(hint, color = Color.LightGray)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(12.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun GenderSelectionSection(
    selectedGender: String,
    onGenderSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Gender",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val genderOptions = listOf("Male", "Female", "Others")

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            genderOptions.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGender == option,
                        onClick = { onGenderSelected(option) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFFF9800),
                            unselectedColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = option,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    ProfileScreen(navController = rememberNavController())
}