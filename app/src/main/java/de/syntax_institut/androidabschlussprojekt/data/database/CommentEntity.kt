package de.syntax_institut.androidabschlussprojekt.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val movieId: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)