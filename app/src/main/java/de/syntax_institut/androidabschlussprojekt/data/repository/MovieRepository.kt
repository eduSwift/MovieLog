package de.syntax_institut.androidabschlussprojekt.data.repository

import de.syntax_institut.androidabschlussprojekt.data.api.APIService
import de.syntax_institut.androidabschlussprojekt.data.database.MovieDao
import de.syntax_institut.androidabschlussprojekt.data.database.MovieEntity
import de.syntax_institut.androidabschlussprojekt.data.model.Movie
import de.syntax_institut.androidabschlussprojekt.data.model.MovieCategory
import de.syntax_institut.androidabschlussprojekt.BuildConfig
import kotlinx.coroutines.flow.Flow


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

    suspend fun deleteMovie(movie: MovieEntity) {
        movieDao.deleteMovie(movie)
    }

    suspend fun getMovieByIdAndUserId(tmdbMovieId: Int, userId: String): MovieEntity? {
        return movieDao.getMovieByIdAndUserId(tmdbMovieId, userId)
    }


    fun getFavoritesForUser(userId: String): Flow<List<MovieEntity>> {
        return movieDao.getFavoriteMovies(userId)
    }

    fun getWantToWatchForUser(userId: String): Flow<List<MovieEntity>> {
        return movieDao.getWantToWatchMovies(userId)
    }

    fun getWatchedForUser(userId: String): Flow<List<MovieEntity>> {
        return movieDao.getWatchedMovies(userId)
    }

    suspend fun deleteAllMoviesByUserId(userId: String) {
        movieDao.deleteAllMoviesByUserId(userId)
    }
}