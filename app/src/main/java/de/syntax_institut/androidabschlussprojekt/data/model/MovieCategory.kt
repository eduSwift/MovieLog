package de.syntax_institut.androidabschlussprojekt.data.model

enum class MovieCategory(val endpoint: String) {
    POPULAR("popular"),
    TOP_RATED("top_rated"),
    UPCOMING("upcoming"),
    NOW_PLAYING("now_playing")
}