package de.syntax_institut.androidabschlussprojekt.navigation

import com.google.android.gms.auth.api.Auth
import de.syntax_institut.androidabschlussprojekt.ui.viewmodels.AuthViewModel
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
    const val MOVIE_DETAIL = "movie_detail/{posterPath}/{title}/{overview}/{releaseDate}"

    fun movieDetailRoute(
        posterPath: String,
        title: String,
        overview: String,
        releaseDate: String
    ): String {
        return "movie_detail/${posterPath.encodeURLPath()}/${title.encodeURLPath()}/${overview.encodeURLPath()}/${releaseDate.encodeURLPath()}"
    }
}