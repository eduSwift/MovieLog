package de.syntax_institut.androidabschlussprojekt.navigation

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun String.encodeURLPath(): String =
    URLEncoder.encode(this, StandardCharsets.UTF_8.toString()).replace("+", "%20")

fun String.decodeURLPath(): String =
    URLDecoder.decode(this, StandardCharsets.UTF_8.toString())

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val AUTH = "profile"
    const val SETTINGS = "settings"
    const val MOVIE_DETAIL = "movie_detail/{movieId}/{posterPath}/{title}/{overview}/{releaseDate}"


    fun movieDetailRoute(
        movieId: Int,
        posterPath: String,
        title: String,
        overview: String,
        releaseDate: String
    ): String {
        val cleanedPosterPath = posterPath.removePrefix("/")

        return "movie_detail/${movieId}/${cleanedPosterPath.encodeURLPath()}/${title.encodeURLPath()}/${overview.encodeURLPath()}/${releaseDate.encodeURLPath()}"
    }
}