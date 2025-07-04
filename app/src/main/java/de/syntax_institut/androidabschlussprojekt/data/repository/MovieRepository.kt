package de.syntax_institut.androidabschlussprojekt.data.repository

import de.syntax_institut.androidabschlussprojekt.BuildConfig
import de.syntax_institut.androidabschlussprojekt.data.api.APIService
import de.syntax_institut.androidabschlussprojekt.data.database.MovieDao
import de.syntax_institut.androidabschlussprojekt.data.database.MovieEntity
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory

class MovieRepository(
    private val api: APIService,
    private val movieDao: MovieDao
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

    suspend fun insertMovie(movie: MovieEntity) {
        movieDao.insertMovie(movie)
    }

    suspend fun getUserMovies(userId: String): List<MovieEntity> {
        return movieDao.getMoviesForUser(userId)
    }

    suspend fun getFavorites(userId: String): List<MovieEntity> {
        return movieDao.getFavorites(userId)
    }

    suspend fun getWantToWatch(userId: String): List<MovieEntity> {
        return movieDao.getWantToWatch(userId)
    }

    suspend fun getWatched(userId: String): List<MovieEntity> {
        return movieDao.getWatched(userId)
    }
}