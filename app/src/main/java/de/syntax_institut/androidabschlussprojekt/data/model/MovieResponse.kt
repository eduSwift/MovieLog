package de.syntax_institut.androidabschlussprojekt.data.model

import android.graphics.Movie

data class MovieResponse(

    val pages: Int,
    val results: List<Movie>,
    val total_pages: Int,
    val total_results: Int
)