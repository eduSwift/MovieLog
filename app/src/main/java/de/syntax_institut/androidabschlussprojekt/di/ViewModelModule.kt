@file:Suppress("DEPRECATION")

package de.syntax_institut.androidabschlussprojekt.di


import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.HomeScreenViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.SearchScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {
    viewModel {
        HomeScreenViewModel(repository = get())
    }

    viewModel {
        SearchScreenViewModel(repository = get())
    }
}