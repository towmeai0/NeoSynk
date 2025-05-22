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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun ProfileDisplay(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val databaseRef = FirebaseDatabase.getInstance().getReference("NeoSynk").child("user")

    LaunchedEffect(userId) {
        userId?.let {
            databaseRef.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    name = snapshot.child("name").getValue(String::class.java) ?: ""
                    loc = snapshot.child("location").getValue(String::class.java) ?: ""
                    gender = snapshot.child("gender").getValue(String::class.java) ?: ""
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
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

                ProfileTextItem(label = "Name", value = name)
                Spacer(modifier = Modifier.height(8.dp))
                ProfileTextItem(label = "Location", value = loc)
                Spacer(modifier = Modifier.height(8.dp))
                ProfileTextItem(label = "Gender", value = gender)
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

@Preview(showBackground = true)
@Composable
fun Scre(){
    ProfileDisplay(navController = rememberNavController())
}