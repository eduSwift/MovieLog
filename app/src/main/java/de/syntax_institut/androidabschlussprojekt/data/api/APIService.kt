package de.syntax_institut.androidabschlussprojekt.data.api

import de.syntax_institut.androidabschlussprojekt.data.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface APIService {
    @GET("movie/{category}")
    suspend fun getMoviesByCategory(
        @Path("category") category: String,
        @Query("api_key") apiKey: String
    ): MovieResponse
}

