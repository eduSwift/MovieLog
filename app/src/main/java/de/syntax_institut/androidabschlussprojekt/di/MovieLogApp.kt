package de.syntax_institut.androidabschlussprojekt.di

import android.app.Application
import com.cloudinary.android.MediaManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MovieLogApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MovieLogApp)
            modules(appModule)
        }
        initCloudinary()
    }
    private fun initCloudinary() {
        val config: MutableMap<String, String> = HashMap()
        config["cloud_name"] = "movielog"
        config["api_key"] = ""
        config["api_secret"] = ""
        MediaManager.init(this, config)
    }
}