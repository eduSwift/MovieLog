package de.syntax_institut.androidabschlussprojekt.data.repository

import de.syntax_institut.androidabschlussprojekt.BuildConfig
import de.syntax_institut.androidabschlussprojekt.data.api.APIService
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory

class MovieRepository(
    private val api: APIService
) {
    suspend fun getMoviesByCategory(category: MovieCategory): List<Movie>{
        return api.getMoviesByCategory(
            category = category.endpoint,
            apiKey = BuildConfig.MOVIE_API_KEY
        ).results
    }

    suspend fun searchMovies(query: String): List<Movie> {
        return api.searchMovies(
            query = query,
            apiKey = BuildConfig.MOVIE_API_KEY
        ).results
    }
}