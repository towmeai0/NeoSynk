package com.ayudevices.neosynkparent.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    var name by mutableStateOf("")
    var number by mutableStateOf("")
    var password by mutableStateOf("")
    var email by mutableStateOf("")
}