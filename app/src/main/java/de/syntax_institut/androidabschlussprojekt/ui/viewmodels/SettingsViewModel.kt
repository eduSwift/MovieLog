package de.syntax_institut.androidabschlussprojekt.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.androidabschlussprojekt.data.datastore.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val isDarkModeEnabled: StateFlow<Boolean> = themePreferences.isDarkModeEnabled
        .map { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDarkModeEnabled(enabled)
        }
    }
}