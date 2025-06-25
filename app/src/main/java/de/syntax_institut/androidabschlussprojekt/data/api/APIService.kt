package de.syntax_institut.androidabschlussprojekt.data.api

import retrofit2.http.Query
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.syntax_institut.androidabschlussprojekt.data.model.MovieResponse
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

const val BASE_URL = "https://api.themoviedb.org/3/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface APIService {
    @GET("movie/upcoming")
    suspend fun getPopularMovies(
        @Query("api_key")
        apiKey: String
    ): MovieResponse
}

object MoviesAPI {
    val api: APIService by lazy { retrofit.create(APIService::class.java) }
}