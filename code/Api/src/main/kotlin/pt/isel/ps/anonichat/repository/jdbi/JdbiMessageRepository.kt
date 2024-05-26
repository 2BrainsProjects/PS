package pt.isel.ps.anonichat.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.ps.anonichat.domain.user.Message
import pt.isel.ps.anonichat.repository.MessageRepository

class JdbiMessageRepository(
    private val handle: Handle
) : MessageRepository {
    /**
     * Saves a message
     * @param userId The user's id
     * @param cid The conversation id
     * @param message The message
     * @param msgDate The message date
     * @return if the message was saved
     */
    override fun saveMessage(userId: Int, cid: String, message: String, msgDate: String): Boolean =
        handle.createUpdate("insert into dbo.Message (user_id, cid, message, msg_date) values (:userId, :cid, :message, TO_TIMESTAMP(:msg_date, 'YYYY-MM-DD HH24:MI:SS'))")
            .bind("userId", userId)
            .bind("cid", cid)
            .bind("message", message)
            .bind("msg_date", msgDate)
            .execute() == 1

    /**
     * Gets the messages
     * @param userId The user's id
     * @param cid The conversation id
     * @return The messages
     */
    override fun getMessages(userId: Int, cid: String) : List<Message> =
        handle.createQuery("select * from dbo.Message where user_id = :userId and cid = :cid")
            .bind("userId", userId)
            .bind("cid", cid)
            .mapTo<Message>()
            .list()

    /**
     * Gets all the messages after a certain date
     * @param userId The user's id
     * @param cid The conversation id
     * @param msgDate The message date
     * @return The messages
     */
    override fun getMessages(userId: Int, cid: String, msgDate: String) : List<Message> =
        handle.createQuery("select * from dbo.Message where user_id = :userId and cid = :cid and msg_date > TO_TIMESTAMP(:msg_date, 'YYYY-MM-DD HH24:MI:SS')")
            .bind("userId", userId)
            .bind("cid", cid)
            .bind("msg_date", msgDate)
            .mapTo<Message>()
            .list()

}