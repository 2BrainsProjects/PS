package pt.isel.ps.anonichat.repository

import pt.isel.ps.anonichat.domain.user.Message

interface MessageRepository {
    fun saveMessage(userId: Int, cid: String, message: String, msgDate: String): Boolean
    fun getMessages(userId: Int, cid: String): List<Message>
    fun getMessages(userId: Int, cid: String, msgDate: String): List<Message>
}