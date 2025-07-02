package de.syntax_institut.androidabschlussprojekt.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MovieLogApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MovieLogApp)
            modules(appModule)
        }
    }
}