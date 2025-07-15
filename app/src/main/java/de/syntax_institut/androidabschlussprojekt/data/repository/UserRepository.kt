package de.syntax_institut.androidabschlussprojekt.data.repository

import de.syntax_institut.androidabschlussprojekt.data.database.UserDao
import de.syntax_institut.androidabschlussprojekt.data.database.UserEntity

class UserRepository(private val userDao: UserDao) {
    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun getUser(uid: String): UserEntity? {
        return userDao.getUserById(uid)
    }

    suspend fun updateNickname(uid: String, nickname: String) {
        userDao.updateNickname(uid, nickname)
    }

    suspend fun updateProfileImageUrl(uid: String, url: String) {
        userDao.updateProfileImageUrl(uid, url)
    }

    suspend fun deleteUser(uid: String) {
        userDao.deleteUser(uid)
    }
}
