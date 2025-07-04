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
}
