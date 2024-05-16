package pt.isel.ps.anonichat.repository

import pt.isel.ps.anonichat.domain.user.Message
import pt.isel.ps.anonichat.domain.user.User

interface UserRepository {
    fun registerUser(name: String, email: String, passwordHash: String): Int
    fun getUser(id: Int): User
    fun getUserByUsername(name: String): User
    fun getUserByEmail(email: String): User
    fun getTotalUsers(): Int
    fun isUser(id: Int): Boolean
    fun isUserByUsername(name: String): Boolean
    fun isUserByEmail(email: String): Boolean
    fun getLastId(): Int
    fun updateIp(id: Int, ip: String): Boolean
    fun updateCert(id: Int, certPath: String): Boolean
    fun saveMessage(userId: Int, cid: String, message: String, msgDate: String): Boolean
    fun getMessages(userId: Int, cid: String): List<Message>
    fun getMessages(userId: Int, cid: String, msgDate: String): List<Message>
    fun getUserSession(id: Int): String
    fun updateSessionInfo(id: Int, sessionInfoPath: String): Boolean
}
