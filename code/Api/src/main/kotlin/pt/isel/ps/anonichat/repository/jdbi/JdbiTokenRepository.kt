package pt.isel.ps.anonichat.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.ps.anonichat.domain.user.Token
import pt.isel.ps.anonichat.domain.user.User
import pt.isel.ps.anonichat.repository.TokenRepository

class JdbiTokenRepository(
    private val handle: Handle
) : TokenRepository {

    /**
     * Creates a new token for the user
     * @param token The token
     * @param maxTokens The maximum number of tokens per user
     */
    override fun createToken(token: Token, maxTokens: Int) {
        handle.createUpdate(
            """
            delete from dbo.Token 
            where user_id = :userId 
                and token_hash in (
                    select token_hash from dbo.Token where user_id = :userId 
                        order by last_used_at desc offset :offset
                )
            """.trimIndent()
        )
            .bind("userId", token.userId)
            .bind("offset", maxTokens - 1)
            .execute()

        handle.createUpdate(
            """
                insert into dbo.Token(token_hash, user_id, created_at, last_used_at) 
                values (:tokenHash, :userId, :createdAt, :lastUsedAt)
            """.trimIndent()
        )
            .bind("tokenHash", token.tokenHash)
            .bind("userId", token.userId)
            .bind("createdAt", token.createdAt.epochSeconds)
            .bind("lastUsedAt", token.lastUsedAt.epochSeconds)
            .execute()
    }

    /**
     * Gets the token hash of the user
     * @param userId The user's id
     * @return The token hash
     */
    override fun getTokenHash(userId: Int): String? =
        handle.createQuery("select token_hash from dbo.Token where user_id = :id")
            .bind("id", userId)
            .mapTo<String>()
            .singleOrNull()

    /**
     * Updates the user's token hash
     * @param id The user's id
     * @param tokenHash The new token hash
     * @return if the token hash was updated
     */
    override fun updateUserTokenHash(id: Int, tokenHash: String): Boolean =
        handle.createUpdate("update dbo.Token set token_hash = :tokenHash where user_id = :id")
            .bind("id", id)
            .bind("tokenHash", tokenHash)
            .executeAndReturnGeneratedKeys()
            .mapTo<String>()
            .one() != null

    /**
     * Gets the user and token by the token hash
     * @param tokenHash The token hash
     * @return The user and token
     */
    override fun getUserAndTokenByTokenHash(tokenHash: String): Pair<User, Token>? {
        val user = handle.createQuery(
            """
                select * from dbo.User as users 
                join dbo.Token as tokens on users.id = tokens.user_id
                where token_hash = :tokenHash
            """.trimIndent()
        )
            .bind("tokenHash", tokenHash)
            .mapTo<User>()
            .singleOrNull() ?: return null

        val token = handle.createQuery("select * from dbo.Token where token_hash = :tokenHash")
            .bind("tokenHash", tokenHash)
            .mapTo<Token>()
            .singleOrNull() ?: return null

        return Pair(user, token)
    }

    /**
     * Updates the token's last used time
     * @param tokenHash The token hash
     * @param now The current time
     * @return if the token was updated
     */
    override fun updateTokenLastUsed(tokenHash: String, now: Instant): Boolean =
        handle.createUpdate(
            """
                update dbo.Token
                set last_used_at = :lastUsedAt
                where token_hash = :tokenHash
            """.trimIndent()
        )
            .bind("lastUsedAt", now.epochSeconds)
            .bind("tokenHash", tokenHash)
            .execute() == 1

    /**
     * Removes the token by the token hash
     * @param tokenHash The token hash
     * @return if the token was removed
     */
    override fun removeTokenByTokenHash(tokenHash: String): Boolean =
        handle.createUpdate("delete from dbo.Token where token_hash = :tokenHash")
            .bind("tokenHash", tokenHash)
            .execute() == 1
}
