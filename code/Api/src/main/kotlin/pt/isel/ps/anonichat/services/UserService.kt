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
import pt.isel.ps.anonichat.domain.user.Message
import pt.isel.ps.anonichat.domain.user.Token
import pt.isel.ps.anonichat.domain.user.User
import pt.isel.ps.anonichat.domain.user.UserDomain
import pt.isel.ps.anonichat.domain.utils.readFromFile
import pt.isel.ps.anonichat.domain.utils.writeToFile
import pt.isel.ps.anonichat.repository.transaction.TransactionManager
import pt.isel.ps.anonichat.services.models.TokenModel
import pt.isel.ps.anonichat.services.models.UserModel.Companion.toModel
import pt.isel.ps.anonichat.services.models.UsersModel
import java.io.File

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
     * @param clientCSR The client's CSR
     * @param path The path of certificate
     * @return The user's id and the certificate
     * @throws UserAlreadyExistsException if the user already exists
     */
    fun registerUser(
        name: String,
        email: String,
        password: String,
        clientCSR: String,
        path: String = basePath
    ): Int {
        val passwordHash = domain.encodePassword(password)
        return tm.run {
            requireOrThrow<UserAlreadyExistsException>(!it.userRepository.isUserByUsername(name)) {
                "User with name $name already exists"
            }
            requireOrThrow<UserAlreadyExistsException>(!it.userRepository.isUserByEmail(email)) {
                "User with email $email already exists"
            }
            // Register a new user
            val userId = it.userRepository.registerUser(name, email, passwordHash)
            val sessionInfoPath = System.getProperty("user.dir") + "\\sessions\\user$userId.txt"
            val file = File(sessionInfoPath)
            File(file.parent).mkdirs()
            file.delete()
            file.createNewFile()

            it.userRepository.updateSessionInfo(userId, sessionInfoPath)

            // Certificate need the user's id to be created
            cd.createCertCommand(clientCSR, userId, path)

            // Update the user with the certificate path
            it.userRepository.updateCert(userId, "$path/$userId.cer")

            userId
        }
    }

    /**
     * Logs in a user
     * @param name The user's username
     * @param email The user's email
     * @param password The user's password
     * @param ip The user's ip
     * @param path The path of certificate
     * @return The user's token and the content of the certificate
     * @throws InvalidCredentialsException if the username or email is not provided
     */
    fun loginUser(
        name: String?,
        email: String?,
        password: String,
        ip: String,
        path: String = basePath
    ): Pair<TokenModel, String> {
        val tokenModelAndSessionInfo: Pair<TokenModel, String>
        when {
            name != null -> {
                tokenModelAndSessionInfo = loginByUsername(name, password, ip)
                tm.run {
                    it.userRepository.getUserByUsername(name).id
                }
            }
            email != null -> {
                tokenModelAndSessionInfo = loginByEmail(email, password, ip)
                tm.run {
                    it.userRepository.getUserByEmail(email).id
                }
            }
            else -> throw InvalidCredentialsException("Username or email is required for login")
        }

        return tokenModelAndSessionInfo
    }

    /**
     * Get list of users
     * @param usersIds list with users ids
     * @return The list of users
     */
    fun getUsers(usersIds: List<Int>): UsersModel {
        return tm.run { tr ->
            val users =
                usersIds.mapNotNull { id ->
                    if (tr.userRepository.isUser(id)) tr.userRepository.getUser(id) else null
                }
                    .map { user ->
                        val cert = if (user.certificate != null) readFromFile(user.certificate) else ""
                        user.toModel(cert)
                    }
            UsersModel(users)
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

    fun getUser(tokenValue: String): User {
        val user = getUserByToken(tokenValue)
        requireOrThrow<UserNotFoundException>(user != null) { "User was not found" }
        return user
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
        val token =
            Token(
                tokenHash = domain.hashToken(tokenValue),
                userId = userId,
                createdAt = now,
                lastUsedAt = now
            )
        tm.run {
            requireOrThrow<UserNotFoundException>(it.userRepository.isUser(userId)) { "User was not found" }
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
     * Save messages of a user in a conversation
     * @param userId The user's id
     * @param cid The conversation id
     * @param message The message
     * @param msgDate The message date
     * @throws UserNotFoundException if the user was not found
     */
    fun saveMessage(
        userId: Int,
        cid: String,
        message: String,
        msgDate: String
    ) = tm.run {
        requireOrThrow<UserNotFoundException>(it.userRepository.isUser(userId)) { "User was not Found" }
        it.messageRepository.saveMessage(userId, cid, message, msgDate)
    }

    /**
     * Get messages if msgDate is null, get all messages, else get messages after msgDate
     * @param userId The user's id
     * @param cid The conversation id
     * @param msgDate The message date
     * @return The list of messages
     * @throws UserNotFoundException if the user was not found
     */
    fun getMessages(
        userId: Int,
        cid: String,
        msgDate: String?
    ): List<Message> =
        tm.run {
            requireOrThrow<UserNotFoundException>(it.userRepository.isUser(userId)) { "User was not Found" }
            if (msgDate != null) {
                it.messageRepository.getMessages(userId, cid, msgDate)
            } else {
                it.messageRepository.getMessages(userId, cid)
            }
        }

    /**
     * Save the session info of a user
     * @param userId The user's id
     * @param sessionInfo The session info
     * @throws UserNotFoundException if the user was not found
     */
    fun saveSessionInfo(
        userId: Int,
        sessionInfo: String
    ) {
        val sessionInfoPath =
            tm.run {
                requireOrThrow<UserNotFoundException>(it.userRepository.isUser(userId)) { "User was not Found" }
                it.userRepository.getUserSession(userId)
            }
        writeToFile(sessionInfoPath, sessionInfo)
    }

    /**
     * Logs in a user by username
     * @param name The user's username
     * @param password The user's password
     * @return The user's token and the session info
     * @throws InvalidCredentialsException if the username or password is incorrect
     */
    private fun loginByUsername(
        name: String,
        password: String,
        ip: String
    ): Pair<TokenModel, String> =
        tm.run {
            requireOrThrow<InvalidCredentialsException>(it.userRepository.isUserByUsername(name)) {
                "Incorrect username or password"
            }
            val user = it.userRepository.getUserByUsername(name)
            requireOrThrow<InvalidCredentialsException>(domain.verifyPassword(password, user.passwordHash)) {
                "Incorrect username or password"
            }
            it.userRepository.updateIp(user.id, ip)
            val sessionInfoPath = it.userRepository.getUserSession(user.id)
            val sessionInfo = readFromFile(sessionInfoPath)

            val tokenModel = createToken(user.id)

            tokenModel to sessionInfo
        }

    /**
     * Logs in a user by email
     * @param email The user's email
     * @param password The user's password
     * @return The user's token and the session info
     * @throws InvalidCredentialsException if the email or password is incorrect
     */
    private fun loginByEmail(
        email: String,
        password: String,
        ip: String
    ): Pair<TokenModel, String> =
        tm.run {
            requireOrThrow<InvalidCredentialsException>(it.userRepository.isUserByEmail(email)) {
                "Incorrect email or password"
            }
            val user = it.userRepository.getUserByEmail(email)
            requireOrThrow<InvalidCredentialsException>(domain.verifyPassword(password, user.passwordHash)) {
                "Incorrect email or password"
            }
            it.userRepository.updateIp(user.id, ip)

            val sessionInfoPath = it.userRepository.getUserSession(user.id)
            val sessionInfo = readFromFile(sessionInfoPath)

            val tokenModel = createToken(user.id)

            tokenModel to sessionInfo
        }

    companion object {
        private val basePath
            get() = path()
        private fun path(): String {
            val userDir = System.getProperty("user.dir")
            val certificatePath = if (userDir == "/" || userDir == "\\") {
                "certificates\\users"
            } else {
                "\\certificates\\users"
            }
            return "$userDir$certificatePath"
        }
    }
}
