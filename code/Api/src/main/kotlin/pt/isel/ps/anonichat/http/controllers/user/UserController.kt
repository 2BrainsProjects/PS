package pt.isel.ps.anonichat.http.controllers.user

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.ps.anonichat.http.controllers.user.models.GetMessageOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetMessagesOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetUserHomeOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetUserInformationOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetUserOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetUsersCountOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetUsersOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.LoginInputModel
import pt.isel.ps.anonichat.http.controllers.user.models.LoginOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.LogoutInputModel
import pt.isel.ps.anonichat.http.controllers.user.models.RegisterInputModel
import pt.isel.ps.anonichat.http.controllers.user.models.RegisterOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.SaveMessagesInputModel
import pt.isel.ps.anonichat.http.media.siren.SirenEntity
import pt.isel.ps.anonichat.http.media.siren.SubEntity
import pt.isel.ps.anonichat.http.pipeline.authentication.RequestTokenProcessor.Companion.TOKEN
import pt.isel.ps.anonichat.http.pipeline.authentication.Session
import pt.isel.ps.anonichat.http.utils.Actions
import pt.isel.ps.anonichat.http.utils.Links
import pt.isel.ps.anonichat.http.utils.MediaTypes.PROBLEM_MEDIA_TYPE
import pt.isel.ps.anonichat.http.utils.MediaTypes.SIREN_MEDIA_TYPE
import pt.isel.ps.anonichat.http.utils.Rels
import pt.isel.ps.anonichat.http.utils.Uris
import pt.isel.ps.anonichat.services.UserService
import pt.isel.ps.anonichat.services.models.TokenModel

@RestController
@RequestMapping(Uris.PREFIX, produces = [SIREN_MEDIA_TYPE, PROBLEM_MEDIA_TYPE])
class UserController(
    val services: UserService
) {
    /**
     * Handles the request to get the user home
     * @param session the user session
     * @return the response with the user home
     */
    @GetMapping(Uris.User.HOME)
    fun getUserHome(
        session: Session
    ): ResponseEntity<*> {
        return SirenEntity(
            clazz = listOf(Rels.User.HOME),
            properties = GetUserHomeOutputModel(
                session.user.id,
                session.user.name
            ),
            links = listOf(
                Links.self(Uris.User.HOME)
            ),
            actions = listOf(
                Actions.User.logout()
            )
        ).ok()
    }

    /**
     * Handles the request to register a new user
     * @param body the request body (RegisterInputModel)
     * @return the response with the user's id and certificate content
     */
    @PostMapping(Uris.User.REGISTER)
    fun registerUser(
        /**
         * removed @RequestBody annotation due to spring doesn´t recognize
         * Content-Type: application/x-www-form-urlencoded as a possible body
         */
        @Valid
        body: RegisterInputModel
    ): ResponseEntity<*> {
        val userId = services.registerUser(body.name, body.email, body.password, body.clientCSR)
        return SirenEntity(
            clazz = listOf(Rels.User.REGISTER),
            properties = RegisterOutputModel(userId)
        ).created(Uris.User.home())
    }

    /**
     * Handles the request to login a user
     * @param body the request body (LoginInputModel)
     * @param response the response of the request
     * @return the response with the user's token
     */
    @PostMapping(Uris.User.LOGIN)
    fun loginUser(
        @Valid
        body: LoginInputModel,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        val (token, sessionInfo) = services.loginUser(body.name, body.email, body.password, body.ip)
        response.addCookie(token)
        return SirenEntity(
            clazz = listOf(Rels.User.LOGIN),
            properties = LoginOutputModel(token.value, token.expiration.epochSeconds, sessionInfo),
            links = listOf(Links.userHome())
        ).ok()
    }

    /**
     * Handles the request to logout a user
     * @param user the user session
     * @param logoutInputModel the request body (LogoutInputModel)
     * @return the response
     */
    @PostMapping(Uris.User.LOGOUT)
    fun logoutUser(
        user: Session,
        logoutInputModel: LogoutInputModel,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        services.saveSessionInfo(user.user.id, logoutInputModel.sessionInfo)
        services.revokeToken(user.token)
        response.removeCookie()
        return SirenEntity<Unit>(
            clazz = listOf(Rels.User.LOGOUT)
        ).ok()
    }

    /**
     * Handles the request to get the user information
     * @param user the user session
     * @return the response with the user information
     */
    @GetMapping(Uris.User.USER)
    fun getUser(user: Session): ResponseEntity<*> {
        val userInfo = services.getUser(user.token)
        return SirenEntity(
            clazz = listOf(Rels.User.USER),
            properties = GetUserOutputModel(userInfo.id, userInfo.name)
        ).ok()
    }

    /**
     * Handles the request to get list of users
     * @param ids the request query ids of the users
     * @return the response with the list of users
     */
    @GetMapping(Uris.User.USERS)
    fun getUsers(
        @RequestParam
        ids: List<Int>
    ): ResponseEntity<*> {
        val (users) = services.getUsers(ids)
        return SirenEntity(
            clazz = listOf(Rels.User.USERS, Rels.Collection.COLLECTION),
            properties = GetUsersOutputModel(users.size),
            links = listOfNotNull(
                Links.self(Uris.User.USERS)
            ),
            entities = users.map { user ->
                SubEntity.EmbeddedRepresentation(
                    rel = listOf(Rels.Collection.ITEM),
                    properties = GetUserInformationOutputModel(user.id, user.ip, user.name, user.certificate)
                )
            }
        ).ok()
    }

    /**
     * Handles the request to get the max users id
     * @return the response with the users max id
     */
    @GetMapping(Uris.User.USERS_COUNT)
    fun getUsersCount(): ResponseEntity<*> {
        val id = services.getLastId()
        return SirenEntity(
            clazz = listOf(Rels.User.USERS_COUNT),
            properties = GetUsersCountOutputModel(id),
            links = listOfNotNull(
                Links.self(Uris.User.USERS_COUNT)
            )
        ).ok()
    }

    /**
     * Handles the request to get the messages of a user
     * @param user the user session
     * @param cid the conversation id
     * @param msgDate the message date
     * @return the response with the user messages
     */
    @GetMapping(Uris.User.MESSAGES)
    fun getMessages(
        user: Session,
        @RequestParam @Valid
        cid: String,
        @Valid
        msgDate: String?
    ): ResponseEntity<*> {
        val messages = services.getMessages(user.user.id, cid, msgDate)
        return SirenEntity(
            clazz = listOf(Rels.User.MESSAGES),
            properties = GetMessagesOutputModel(messages.count()),
            links = listOfNotNull(
                Links.self(Uris.User.MESSAGES)
            ),
            entities = messages.map { message ->
                SubEntity.EmbeddedRepresentation(
                    rel = listOf(Rels.Collection.ITEM),
                    properties = GetMessageOutputModel(message.cid, message.message, message.msgDate)
                )
            }
        ).ok()
    }

    /**
     * Handles the request to save messages of a user
     * @param user the user session
     * @param body the request body (SaveMessagesInputModel)
     * @return the response
     */
    @PostMapping(Uris.User.MESSAGES)
    fun saveMessage(
        user: Session,
        body: SaveMessagesInputModel
    ): ResponseEntity<*> {
        services.saveMessage(user.user.id, body.cid, body.message, body.msgDate)
        return SirenEntity<Unit>(
            clazz = listOf(Rels.User.MESSAGES),
            links = listOfNotNull(
                Links.self(Uris.User.MESSAGES)
            )
        ).ok()
    }

    private fun HttpServletResponse.addCookie(token: TokenModel) {
        val cookie = Cookie(TOKEN, token.value).also {
            it.isHttpOnly = true
            it.maxAge = token.expiration.epochSeconds.toInt()
            it.path = Uris.PREFIX
        }
        addCookie(cookie)
    }

    private fun HttpServletResponse.removeCookie() {
        val cookie = Cookie(TOKEN, "").also {
            it.isHttpOnly = true
            it.maxAge = 0
            it.path = Uris.PREFIX
        }
        addCookie(cookie)
    }
}
