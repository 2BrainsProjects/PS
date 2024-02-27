package pt.isel.daw.gomoku.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.daw.gomoku.domain.user.User
import pt.isel.daw.gomoku.repository.UserRepository

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
        handle.createUpdate("insert into dbo.User (name, email, password_hash) values (:name, :email, :passwordHash)")
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
     * Gets a list of users
     * @param skip The number of users to skip
     * @param limit the max number of users to include
     * @param orderBy the column to order by
     * @param sort the sort order
     * @return the list of users
     */
    override fun getUsers(skip: Int, limit: Int, orderBy: String, sort: String): List<User> =
        handle.createQuery(
            """
                select * from dbo.User
                order by $orderBy $sort, name asc
                offset :skip rows
                limit :limit
            """.trimIndent()
        )
            .bind("skip", skip)
            .bind("limit", limit)
            .mapTo<User>()
            .list()

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
}
