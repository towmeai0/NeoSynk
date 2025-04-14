package com.example.neosynk.viewmodel

import androidx.lifecycle.ViewModel
import com.example.neosynk.ui.screen.Tab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _selectedTab = MutableStateFlow(Tab.LIVE_FEED)
    val selectedTab: StateFlow<Tab> = _selectedTab

    fun selectTab(tab: Tab) {
        _selectedTab.value = tab
    }
}
