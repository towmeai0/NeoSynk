package com.ayudevices.neosynkparent.viewmodel

import androidx.lifecycle.ViewModel
import com.ayudevices.neosynkparent.data.network.TokenSender
import com.ayudevices.neosynkparent.ui.screen.Tab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenSender: TokenSender
) : ViewModel() {
    private val _selectedTab = MutableStateFlow(Tab.LIVE_FEED)
    val selectedTab: StateFlow<Tab> = _selectedTab

    fun selectTab(tab: Tab) {
        _selectedTab.value = tab
    }

    fun reqVitals(){
        tokenSender.requestVitals()
    }
}
