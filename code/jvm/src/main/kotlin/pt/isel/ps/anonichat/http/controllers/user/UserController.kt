package pt.isel.ps.anonichat.http.controllers.user

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.ps.anonichat.domain.utils.Ip
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersCountOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetUserOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetUsersCountOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetUsersInputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetUsersOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.LoginInputModel
import pt.isel.ps.anonichat.http.controllers.user.models.LoginOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.RegisterInputModel
import pt.isel.ps.anonichat.http.controllers.user.models.RegisterOutputModel
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
            properties = GetUserOutputModel(
                session.user.id,
                session.user.ip ?: "",
                session.user.name,
                session.user.certificate ?: ""  // problema de registo noutro sitio
            ),
            links = listOf(
                Links.self(Uris.User.HOME),
                Links.home()
            ),
            actions = listOf(
                Actions.User.logout()
            )
        ).ok()
    }

    /**
     * Handles the request to register a new user
     * @param body the request body (RegisterInputModel)
     * @return the response with the user's id
     */
    @PostMapping(Uris.User.REGISTER)
    fun registerUser(
        @Valid @RequestBody
        body: RegisterInputModel
    ): ResponseEntity<*> {
        val (userId, certificateContent) = services.registerUser(body.name, body.email, body.password, body.publicKey)
        return SirenEntity(
            clazz = listOf(Rels.User.REGISTER),
            properties = RegisterOutputModel(userId, certificateContent),
            links = listOf(Links.home())
        ).created(Uris.User.home())
    }

    /**
     * Handles the request to login a user
     * @param body the request body (LoginInputModel)
     * @param
     * @return the response with the user's token
     */
    @PostMapping(Uris.User.LOGIN)
    fun loginUser(
        @Valid @RequestBody
        body: LoginInputModel,
        response: HttpServletResponse,
        ip: Ip
    ): ResponseEntity<*> {
        val token = services.loginUser(body.name, body.email, body.password, ip.ip)
        response.addCookie(token)
        return SirenEntity(
            clazz = listOf(Rels.User.LOGIN),
            properties = LoginOutputModel(token.value, token.expiration.epochSeconds),
            links = listOf(Links.home(), Links.userHome())
        ).ok()
    }

    /**
     * Handles the request to logout a user
     * @param user the user session
     * @return the response
     */
    @PostMapping(Uris.User.LOGOUT)
    fun logoutUser(user: Session, response: HttpServletResponse): ResponseEntity<*> {
        services.revokeToken(user.token)
        response.removeCookie()
        return SirenEntity<Unit>(
            clazz = listOf(Rels.User.LOGOUT),
            links = listOf(Links.home())
        ).ok()
    }

    /**
     * Handles the request to get list of users
     * @param body the request body (GetUsersInputModel)
     * @return the response with the list of users
     */
    @GetMapping(Uris.User.USERS)
    fun getUsers(
        @RequestBody
        body: GetUsersInputModel
    ): ResponseEntity<*> {
        val (users, count) = services.getUsers(body.usersIdList)
        return SirenEntity(
            clazz = listOf(Rels.User.USERS, Rels.Collection.COLLECTION),
            properties = GetUsersOutputModel(count),
            links = listOfNotNull(
                Links.self(Uris.User.USERS)
            ),
            entities = users.map { user ->
                SubEntity.EmbeddedRepresentation(
                    rel = listOf(Rels.Collection.ITEM),
                    properties = GetUserOutputModel(user.id, user.ip, user.name, user.certificate)
                )
            }
        ).ok()
    }

    @GetMapping(Uris.User.USERS_COUNT)
    fun getRoutersCount(): ResponseEntity<*> {
        val id = services.getLastId()
        return SirenEntity(
            clazz = listOf(Rels.User.USERS_COUNT),
            properties = GetUsersCountOutputModel(id),
            links = listOfNotNull(
                Links.self(Uris.User.USERS_COUNT)
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
