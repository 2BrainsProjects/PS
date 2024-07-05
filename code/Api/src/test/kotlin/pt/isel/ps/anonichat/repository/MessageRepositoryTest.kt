package pt.isel.ps.anonichat.repository

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MessageRepositoryTest : RepositoryTest() {

    @Test
    fun `save and get message`() {
        val username = testUsername()
        val email = testEmail()
        val password = HASHED_TEST_PASSWORD

        val userId = usersRepository.registerUser(username, email, password)
        val cid = testCid()
        val message1 = "hello, how you doing?"
        val message2 = "Well and you?"
        val msgDate1 = testTimestamp()

        assertTrue(messagesRepository.saveMessage(userId, cid, message1, msgDate1))
        Thread.sleep(1000)

        val msgDate2 = testTimestamp()
        messagesRepository.saveMessage(userId, cid, message2, msgDate2)

        val messages = messagesRepository.getMessages(userId, cid)
        assertEquals(2, messages.size)

        val msg = messages.firstOrNull { it.message == message2 }
        assertNotNull(msg)
        assertEquals(message2, msg.message)
        assertEquals(userId, msg.userId)
        assertEquals(cid, msg.cid)
        assertEquals(msgDate2.dropLastWhile { it == '0' }, msg.msgDate)
        assertNotNull(messages.firstOrNull { it.message == message1 })

        val messagesWithTime = messagesRepository.getMessages(userId, cid, msgDate1)

        assertEquals(1, messagesWithTime.size)
        val msgWithTime = messagesWithTime.first()
        assertEquals(message2, msgWithTime.message)
        assertEquals(userId, msgWithTime.userId)
        assertEquals(cid, msgWithTime.cid)
        assertEquals(msgDate2.dropLastWhile { it == '0' }, msgWithTime.msgDate)
    }
}
