package pt.isel.ps.anonichat.repository

import pt.isel.ps.anonichat.domain.user.User
import java.security.cert.Certificate

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
    fun updateIp(id: Int, ip: String): Boolean
    fun updateCert(id: Int): Boolean
}
