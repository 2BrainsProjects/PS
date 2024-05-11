package pt.isel.ps.anonichat.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.ps.anonichat.domain.user.Message
import pt.isel.ps.anonichat.domain.user.User
import pt.isel.ps.anonichat.repository.UserRepository

class JdbiUserRepository(
    private val handle: Handle
) : UserRepository {

    /**
     * Registers a new user
     * @param name The user's name
     * @param email The user's email
     * @param passwordHash The user's password hash
     * @return The user's id
     */
    override fun registerUser(name: String, email: String, passwordHash: String): Int =
        handle.createUpdate("insert into dbo.User (ip, name, email, password_hash, certificate) values (null, :name, :email, :passwordHash, null)")
            .bind("name", name)
            .bind("email", email)
            .bind("passwordHash", passwordHash)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .single()

    /**
     * Gets a user by id
     * @param id The user's id
     * @return The user
     */
    override fun getUser(id: Int): User =
        handle.createQuery("select * from dbo.User where id = :id")
            .bind("id", id)
            .mapTo<User>()
            .single()

    /**
     * Gets a user by name
     * @param name The user's name
     * @return The user
     */
    override fun getUserByUsername(name: String): User =
        handle.createQuery("select * from dbo.User where name = :name")
            .bind("name", name)
            .mapTo<User>()
            .single()

    /**
     * Gets a user by email
     * @param email The user's email
     * @return The user
     */
    override fun getUserByEmail(email: String): User =
        handle.createQuery("select * from dbo.User where email = :email")
            .bind("email", email)
            .mapTo<User>()
            .single()

    /**
     * Gets the number of total users
     * @return total users
     */
    override fun getTotalUsers(): Int =
        handle.createQuery("select count(*) from dbo.User")
            .mapTo<Int>()
            .one()

    /**
     * Check is a user exists in the database by id
     * @param id The user's id
     * @return if the user exists
     */
    override fun isUser(id: Int): Boolean =
        handle.createQuery("select count(*) from dbo.User where id = :id")
            .bind("id", id)
            .mapTo<Int>()
            .one() == 1

    /**
     * Check is a user exists in the database by name
     * @param name The user's name
     * @return if the user exists
     */
    override fun isUserByUsername(name: String): Boolean =
        handle.createQuery("select count(*) from dbo.User where name = :username")
            .bind("username", name)
            .mapTo<Int>()
            .one() == 1

    /**
     * Check is a user exists in the database by email
     * @param email The user's email
     * @return if the user exists
     */
    override fun isUserByEmail(email: String): Boolean =
        handle.createQuery("select count(*) from dbo.User where email = :email")
            .bind("email", email)
            .mapTo<Int>()
            .one() == 1

    /**
     * Gets the last user's id
     * @return The last user's id
     */
    override fun getLastId(): Int =
        handle.createQuery("select max(id) from dbo.User")
            .mapTo<Int>()
            .one()

    /**
     * Updates a user's ip
     * @param id The user's id
     * @param ip The user's ip
     * @return if the user's ip was updated
     */
    override fun updateIp(id: Int, ip: String): Boolean =
        handle.createUpdate("update dbo.User set ip = :ip where id = :id")
            .bind("id", id)
            .bind("ip", ip)
            .execute() == 1

    /**
     * Updates a user's certificate
     * @param id The user's id
     * @return if the user's certificate was updated
     */
    override fun updateCert(id: Int, certPath: String): Boolean =
        handle.createUpdate("update dbo.User set certificate = :certPath where id = :id")
            .bind("id", id)
            .bind("certPath", certPath)
            .execute() == 1

    override fun saveMessages(userId: Int, cid: String, message: String, msgDate: String): Boolean =
        handle.createUpdate("insert into dbo.Message (user_id, cid, message, msg_date) values (:userId, :cid, :message, TO_TIMESTAMP(:msg_date, 'YYYY-MM-DD HH24:MI:SS'))")
            .bind("userId", userId)
            .bind("cid", cid)
            .bind("message", message)
            .bind("msg_date", msgDate)
            .execute() == 1

    override fun getMessages(userId: Int, cid: String) : List<Message> =
        handle.createQuery("select * from dbo.Message where user_id = :userId and cid = :cid")
            .bind("userId", userId)
            .bind("cid", cid)
            .mapTo<Message>()
            .list()

    override fun getMessages(userId: Int, cid: String, msgDate: String) : List<Message> =
        handle.createQuery("select * from dbo.Message where user_id = :userId and cid = :cid and msg_date > TO_TIMESTAMP(:msg_date, 'YYYY-MM-DD HH24:MI:SS')")
            .bind("userId", userId)
            .bind("cid", cid)
            .bind("msg_date", msgDate)
            .mapTo<Message>()
            .list()

    override fun getUserSession(id: Int): String =
        handle.createQuery("select session_info from dbo.User where id = :id")
            .bind("id", id)
            .mapTo<String>()
            .one()

    override fun updateSessionInfo(id: Int, sessionInfoPath: String): Boolean =
        handle.createUpdate("update dbo.User set session_info = :sessionInfoPath where id = :id")
            .bind("id", id)
            .bind("sessionInfoPath", sessionInfoPath)
            .execute() == 1
}
