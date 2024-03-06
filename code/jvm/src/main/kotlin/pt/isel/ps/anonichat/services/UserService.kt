package pt.isel.ps.anonichat.services

import kotlinx.datetime.Clock
import org.springframework.stereotype.Component
import pt.isel.ps.anonichat.domain.certificate.CertificateDomain
import pt.isel.ps.anonichat.domain.exceptions.UserException.InvalidCredentialsException
import pt.isel.ps.anonichat.domain.exceptions.UserException.InvalidTokenException
import pt.isel.ps.anonichat.domain.exceptions.UserException.UnauthorizedException
import pt.isel.ps.anonichat.domain.exceptions.UserException.UserAlreadyExistsException
import pt.isel.ps.anonichat.domain.exceptions.UserException.UserNotFoundException
import pt.isel.ps.anonichat.domain.exceptions.requireOrThrow
import pt.isel.ps.anonichat.domain.user.Token
import pt.isel.ps.anonichat.domain.user.User
import pt.isel.ps.anonichat.domain.user.UserDomain
import pt.isel.ps.anonichat.repository.transaction.TransactionManager
import pt.isel.ps.anonichat.services.models.TokenModel
import pt.isel.ps.anonichat.services.models.UserModel.Companion.toModel
import pt.isel.ps.anonichat.services.models.UsersModel

@Component
class UserService(
    private val tm: TransactionManager,
    private val domain: UserDomain,
    private val cd: CertificateDomain,
    private val clock: Clock
) {
    /**
     * Registers a new user
     * @param name The user's name
     * @param email The user's email
     * @param password The user's password
     * @return The user's id
     * @throws UserAlreadyExistsException if the user already exists
     */
    fun registerUser(name: String, email: String, password: String, publicKey: String): Pair<Int, String> {
        val passwordHash = domain.encodePassword(password)
        return tm.run {
            requireOrThrow<UserAlreadyExistsException>(!it.userRepository.isUserByUsername(name)) {
                "User with name $name already exists"
            }
            requireOrThrow<UserAlreadyExistsException>(!it.userRepository.isUserByEmail(email)) {
                "User with email $email already exists"
            }
            val userId = it.userRepository.registerUser(name, email, passwordHash)
            val certContent = cd.createKeyCommand(publicKey, userId, name, email, password)
            it.userRepository.updateCert(userId, "$BASE_PATH/users/$userId.crt")

            Pair(userId, certContent)
        }
    }

    /**
     * Logs in a user
     * @param name The user's username
     * @param email The user's email
     * @param password The user's password
     * @return The user's token
     * @throws InvalidCredentialsException if the username or email is not provided
     */
    fun loginUser(name: String?, email: String?, password: String, ip: String): TokenModel {
        val tokenModel = when {
            name != null -> loginByUsername(name, password, ip)
            email != null -> loginByEmail(email, password, ip)
            else -> throw InvalidCredentialsException("Username or email is required for login")
        }

        return tokenModel
    }

    /**
     * Get list of users
     * @param skip The number of users to skip
     * @param limit The number of users to get
     * @param orderBy The column to order by
     * @param sort The sort order
     * @return The list of users
     */
    fun getUsers(usersIds: List<Int>): UsersModel {
        return tm.run { tr ->
            val users = usersIds.mapNotNull { id ->
                if (tr.userRepository.isUser(id)) {
                    tr.userRepository.getUser(id)
                }else
                    null
            }
                .map { user ->
                    val cert = if(user.certificate != null) {
                        cd.readFile(user.certificate)
                    } else ""
                    user.toModel(cert)
                }
            val maxUserId = tr.userRepository.getLastId()
            UsersModel(users, maxUserId)
        }
    }

    /**
     * Gets a user by token
     * @param tokenValue The token's value
     * @return The user
     * @throws UnauthorizedException if the token is invalid or expired
     */
    fun getUserByToken(tokenValue: String): User? {
        requireOrThrow<UnauthorizedException>(domain.isToken(tokenValue)) { "Invalid user token" }
        val tokenHash = domain.hashToken(tokenValue)
        return tm.run {
            it.tokenRepository.getUserAndTokenByTokenHash(tokenHash)?.let { (user, token) ->
                requireOrThrow<UnauthorizedException>(!domain.hasTokenExpired(token, clock)) { "The provided user token is expired" }
                it.tokenRepository.updateTokenLastUsed(tokenHash, clock.now())
                user
            }
        }
    }

    /**
     * Creates a new valid token for a user
     * @param userId The user's id
     * @return The token model containing its value and expiration
     * @throws UserNotFoundException if the user was not found
     */
    private fun createToken(userId: Int): TokenModel {
        val tokenValue = domain.generateTokenValue()
        val now = clock.now()
        val token = Token(
            tokenHash = domain.hashToken(tokenValue),
            userId = userId,
            createdAt = now,
            lastUsedAt = now
        )
        tm.run {
            requireOrThrow<UserNotFoundException>(it.userRepository.isUser(userId)) {
                "User was not found"
            }
            it.tokenRepository.createToken(token, domain.maxTokensPerUser)
        }
        return TokenModel(
            value = tokenValue,
            expiration = domain.getTokenExpiration(token)
        )
    }

    /**
     * Revokes a token
     * @param tokenValue The token's value
     * @return if the token was revoked
     * @throws InvalidTokenException if the token is invalid
     */
    fun revokeToken(tokenValue: String): Boolean {
        requireOrThrow<InvalidTokenException>(domain.isToken(tokenValue)) { "Token is invalid" }
        val tokenHash = domain.hashToken(tokenValue)
        return tm.run {
            it.tokenRepository.removeTokenByTokenHash(tokenHash)
        }
    }

    /**
     * Gets the last router id
     * @return The last router id
     */
    fun getLastId(): Int {
        return tm.run {
            it.userRepository.getLastId()
        }
    }

    /**
     * Logs in a user by username
     * @param name The user's username
     * @param password The user's password
     * @return The user's token
     * @throws InvalidCredentialsException if the username or password is incorrect
     */
    private fun loginByUsername(name: String, password: String, ip: String): TokenModel = tm.run {
        requireOrThrow<InvalidCredentialsException>(it.userRepository.isUserByUsername(name)) {
            "Incorrect username or password"
        }
        val user = it.userRepository.getUserByUsername(name)
        requireOrThrow<InvalidCredentialsException>(domain.verifyPassword(password, user.passwordHash)) {
            "Incorrect username or password"
        }
        it.userRepository.updateIp(user.id, ip)

        createToken(user.id)
    }

    /**
     * Logs in a user by email
     * @param email The user's email
     * @param password The user's password
     * @return The user's token
     * @throws InvalidCredentialsException if the email or password is incorrect
     */
    private fun loginByEmail(email: String, password: String, ip: String): TokenModel = tm.run {
        requireOrThrow<InvalidCredentialsException>(it.userRepository.isUserByEmail(email)) {
            "Incorrect email or password"
        }
        val user = it.userRepository.getUserByEmail(email)
        requireOrThrow<InvalidCredentialsException>(domain.verifyPassword(password, user.passwordHash)) {
            "Incorrect email or password"
        }
        it.userRepository.updateIp(user.id, ip)

        createToken(user.id)
    }

    companion object{
        private const val BASE_PATH = "/certificate"
    }
}
