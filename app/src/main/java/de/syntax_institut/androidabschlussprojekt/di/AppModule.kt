package de.syntax_institut.androidabschlussprojekt.di

import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.syntax_institut.androidabschlussprojekt.data.api.APIService
import de.syntax_institut.androidabschlussprojekt.data.database.AppDatabase
import de.syntax_institut.androidabschlussprojekt.data.database.CommentDao
import de.syntax_institut.androidabschlussprojekt.data.database.MovieDao
import de.syntax_institut.androidabschlussprojekt.data.database.UserDao
import de.syntax_institut.androidabschlussprojekt.data.datastore.ThemePreferences
import de.syntax_institut.androidabschlussprojekt.data.repository.CommentRepository
import de.syntax_institut.androidabschlussprojekt.data.repository.MovieRepository
import de.syntax_institut.androidabschlussprojekt.data.repository.UserRepository
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.CommentViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.HomeScreenViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.MovieViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.ProfileViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.SearchScreenViewModel
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

const val BASE_URL = "https://api.themoviedb.org/3/"

val appModule = module {

    single {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
    }

    single<APIService> {
        get<Retrofit>().create(APIService::class.java)
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_master_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    single<UserDao> { get<AppDatabase>().userDao() }
    single<MovieDao> { get<AppDatabase>().movieDao() }
    single<CommentDao> { get<AppDatabase>().commentDao()}

    single { UserRepository(get()) }
    single { MovieRepository(api = get(), movieDao = get()) }
    single { ThemePreferences(get()) }
    single { CommentRepository(get()) }

    viewModel { HomeScreenViewModel(get()) }
    viewModel { SearchScreenViewModel(get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { MovieViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { CommentViewModel(get(), get()) }

}
