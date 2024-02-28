package pt.isel.daw.gomoku.services

import kotlinx.datetime.Clock
import org.springframework.stereotype.Component
import pt.isel.daw.gomoku.domain.exceptions.UserException.InvalidCredentialsException
import pt.isel.daw.gomoku.domain.exceptions.UserException.InvalidTokenException
import pt.isel.daw.gomoku.domain.exceptions.UserException.UnauthorizedException
import pt.isel.daw.gomoku.domain.exceptions.UserException.UserAlreadyExistsException
import pt.isel.daw.gomoku.domain.exceptions.UserException.UserNotFoundException
import pt.isel.daw.gomoku.domain.exceptions.requireOrThrow
import pt.isel.daw.gomoku.domain.user.Token
import pt.isel.daw.gomoku.domain.user.User
import pt.isel.daw.gomoku.domain.user.UserDomain
import pt.isel.daw.gomoku.repository.transaction.TransactionManager
import pt.isel.daw.gomoku.services.models.TokenModel
import pt.isel.daw.gomoku.services.models.UserModel
import pt.isel.daw.gomoku.services.models.UserModel.Companion.toModel
import pt.isel.daw.gomoku.services.models.UsersModel

@Component
class UserService(
    private val tm: TransactionManager,
    private val domain: UserDomain,
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
    fun registerUser(name: String, email: String, password: String, publicKey: String): Int {
        val passwordHash = domain.encodePassword(password)
        // pode mandar exceção
        val serverCertificate = domain.loadServerCertificate()
        val signedPublicKey = domain.signPublicKey(publicKey, serverCertificate)   // enviar de volta para o cliente?
            return tm.run {
                requireOrThrow<UserAlreadyExistsException>(!it.userRepository.isUserByUsername(name)) {
                    "User with name $name already exists"
                }
                requireOrThrow<UserAlreadyExistsException>(!it.userRepository.isUserByEmail(email)) {
                    "User with email $email already exists"
                }
                it.userRepository.registerUser(name, email, passwordHash)
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
    fun loginUser(name: String?, email: String?, password: String): TokenModel =
        when {
            name != null -> loginByUsername(name, password)
            email != null -> loginByEmail(email, password)
            else -> throw InvalidCredentialsException("Username or email is required for login")
        }

    /**
     * Gets a user by id
     * @param id The user's id
     * @return The user
     * @throws UserNotFoundException if the user was not found
     */
    fun getUser(id: Int): UserModel = tm.run {
        requireOrThrow<UserNotFoundException>(it.userRepository.isUser(id)) { "User was not found" }
        it.userRepository.getUser(id).toModel()
    }

    /**
     * Get list of users
     * @param skip The number of users to skip
     * @param limit The number of users to get
     * @param orderBy The column to order by
     * @param sort The sort order
     * @return The list of users
     */
    fun getUsers(skip: Int, limit: Int, orderBy: String, sort: String): UsersModel {
        return tm.run {
            val users = it.userRepository.getUsers(skip, limit, orderBy, sort).map { user -> user.toModel() }
            val totalUsers = it.userRepository.getTotalUsers()
            UsersModel(users, totalUsers)
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
     * Logs in a user by username
     * @param name The user's username
     * @param password The user's password
     * @return The user's token
     * @throws InvalidCredentialsException if the username or password is incorrect
     */
    private fun loginByUsername(name: String, password: String): TokenModel = tm.run {
        requireOrThrow<InvalidCredentialsException>(it.userRepository.isUserByUsername(name)) {
            "Incorrect username or password"
        }
        val user = it.userRepository.getUserByUsername(name)
        requireOrThrow<InvalidCredentialsException>(domain.verifyPassword(password, user.passwordHash)) {
            "Incorrect username or password"
        }
        createToken(user.id)
    }

    /**
     * Logs in a user by email
     * @param email The user's email
     * @param password The user's password
     * @return The user's token
     * @throws InvalidCredentialsException if the email or password is incorrect
     */
    private fun loginByEmail(email: String, password: String): TokenModel = tm.run {
        requireOrThrow<InvalidCredentialsException>(it.userRepository.isUserByEmail(email)) {
            "Incorrect email or password"
        }
        val user = it.userRepository.getUserByEmail(email)
        requireOrThrow<InvalidCredentialsException>(domain.verifyPassword(password, user.passwordHash)) {
            "Incorrect email or password"
        }
        createToken(user.id)
    }
}
