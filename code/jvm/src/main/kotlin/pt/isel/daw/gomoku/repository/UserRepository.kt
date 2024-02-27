package pt.isel.daw.gomoku.repository

import pt.isel.daw.gomoku.domain.user.User

interface UserRepository {

    fun registerUser(name: String, email: String, passwordHash: String): Int
    fun getUser(id: Int): User
    fun getUserByUsername(name: String): User
    fun getUserByEmail(email: String): User
    fun getUsers(skip: Int, limit: Int, orderBy: String, sort: String): List<User>
    fun getTotalUsers(): Int
    fun isUser(id: Int): Boolean
    fun isUserByUsername(name: String): Boolean
    fun isUserByEmail(email: String): Boolean
}
