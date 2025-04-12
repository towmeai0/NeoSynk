package com.example.neosynk.ui.screen.bottomNavScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.neosynk.viewmodel.ChatViewModel
import com.example.neosynk.data.ChatEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiyaScreen(navController: NavController, viewModel: ChatViewModel = viewModel()) {
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    val messages by remember { derivedStateOf { viewModel.messages } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        TopAppBar(
            title = {
                Text("Chat", color = Color.White, fontSize = 20.sp)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.DarkGray)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                ChatBubble(message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray, RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userInput,
                onValueChange = { userInput = it },
                placeholder = { Text("Type a message...") },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier.weight(1f),
                maxLines = 2,
                singleLine = false
            )

            IconButton(onClick = {
                val text = userInput.text.trim()
                if (text.isNotEmpty()) {
                    viewModel.sendMessage(text)
                    userInput = TextFieldValue("")
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatEntity) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) Color(0xFF4CAF50) else Color(0xFF333333)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(text = message.message, color = Color.White)
        }
    }
}
