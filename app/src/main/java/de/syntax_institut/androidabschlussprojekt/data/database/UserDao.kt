package de.syntax_institut.androidabschlussprojekt.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserById(uid: String): UserEntity?

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun observeUserById(uid: String): kotlinx.coroutines.flow.Flow<UserEntity?>

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUser(uid: String)

    @Query("UPDATE users SET nickname = :nickname WHERE uid = :uid")
    suspend fun updateNickname(uid: String, nickname: String)

}