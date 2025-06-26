package de.syntax_institut.androidabschlussprojekt.data.repository

import android.R.attr.apiKey
import de.syntax_institut.androidabschlussprojekt.BuildConfig
import de.syntax_institut.androidabschlussprojekt.data.api.MoviesAPI
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory

class MovieRepository {

    suspend fun getMoviesByCategory(category: MovieCategory): List<Movie>{
        return MoviesAPI.api.getMoviesByCategory(
            category = category.endpoint,
            apiKey = BuildConfig.MOVIE_API_KEY
        ).results
    }
}