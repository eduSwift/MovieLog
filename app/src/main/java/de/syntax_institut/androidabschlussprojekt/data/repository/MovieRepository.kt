package de.syntax_institut.androidabschlussprojekt.data.repository

import de.syntax_institut.androidabschlussprojekt.data.api.MoviesAPI
import de.syntax_institut.androidabschlussprojekt.data.model.Movie

class MovieRepository {
    suspend fun getPopularMoviesFromOnlineApi(apiKey: String): List<Movie>{
        return MoviesAPI.api.getPopularMovies(apiKey).results
    }
}