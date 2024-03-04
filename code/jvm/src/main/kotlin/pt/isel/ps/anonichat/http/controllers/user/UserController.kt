package pt.isel.ps.anonichat.http.controllers.user

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.ps.anonichat.http.controllers.user.models.GetUserOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.GetUsersCountOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.LoginInputModel
import pt.isel.ps.anonichat.http.controllers.user.models.LoginOutputModel
import pt.isel.ps.anonichat.http.controllers.user.models.RegisterInputModel
import pt.isel.ps.anonichat.http.media.siren.SirenEntity
import pt.isel.ps.anonichat.http.media.siren.SubEntity
import pt.isel.ps.anonichat.http.pipeline.authentication.RequestTokenProcessor.Companion.TOKEN
import pt.isel.ps.anonichat.http.pipeline.authentication.Session
import pt.isel.ps.anonichat.http.utils.Actions
import pt.isel.ps.anonichat.http.utils.Links
import pt.isel.ps.anonichat.http.utils.MediaTypes.PROBLEM_MEDIA_TYPE
import pt.isel.ps.anonichat.http.utils.MediaTypes.SIREN_MEDIA_TYPE
import pt.isel.ps.anonichat.http.utils.Params
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
    fun getUserHome(session: Session): ResponseEntity<*> =
        SirenEntity(
            clazz = listOf(Rels.User.HOME),
            properties = GetUserOutputModel(
                session.user.name,
                session.user.email
            ),
            links = listOf(
                Links.self(Uris.User.HOME),
                Links.home()
            ),
            actions = listOf(
                Actions.User.logout(),
                Actions.User.getById(session.user.id)
            )
        ).ok()

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
        val userId = services.registerUser(body.name, body.email, body.password, body.publicKey)
        return SirenEntity<Unit>(
            clazz = listOf(Rels.User.REGISTER),
            // properties = RegisterOutputModel(CertificateFactory.getInstance("X.509").generateCertificate("".byteInputStream())),
            links = listOf(Links.home()),
            entities = listOf(
                SubEntity.EmbeddedLink(
                    rel = listOf(Rels.User.USER),
                    href = Uris.User.user(userId)
                )
            )
        ).created(Uris.User.user(userId))
    }

    /**
     * Handles the request to login a user
     * @param body the request body (LoginInputModel)
     * @return the response with the user's token
     */
    @PostMapping(Uris.User.LOGIN)
    fun loginUser(
        @Valid @RequestBody
        body: LoginInputModel,
        response: HttpServletResponse
    ): ResponseEntity<*> {
        val token = services.loginUser(body.name, body.email, body.password, body.ip, body.certificate)
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
     * Handles the request to get a user by id
     * @param userId the user's id
     * @return the response with the user
     */
    @GetMapping(Uris.User.USER)
    fun getUser(@PathVariable userId: Int): ResponseEntity<*> {
        val user = services.getUser(userId)
        return SirenEntity(
            clazz = listOf(Rels.User.USER),
            properties = GetUserOutputModel(user.name, user.email),
            links = listOf(Links.self(Uris.User.user(userId).toString())),
            actions = listOf()
        ).ok()
    }

    /**
     * Handles the request to get list of users
     * @param page the page number
     * @param limit the max number of users to include
     * @param orderBy the property to order by
     * @param sort the sort order
     * @return the response with the list of users
     */
    @GetMapping(Uris.User.USERS)
    fun getUsers(
        @RequestParam page: Int? = null,
        @RequestParam limit: Int? = null,
        @RequestParam sort: String? = null,
        @RequestParam orderBy: String? = null
    ): ResponseEntity<*> {
        val params = Params(page, limit, sort, orderBy)
        val (users, totalUsers) = services.getUsers(params.skip, params.limit, params.orderBy, params.sort)
        return SirenEntity(
            clazz = listOf(Rels.User.USERS, Rels.Collection.COLLECTION),
            properties = GetUsersCountOutputModel(users.size),
            links = listOfNotNull(
                Links.self(Uris.User.USERS),
                params.getPrevPageLink(Uris.User.users()),
                params.getNextPageLink(Uris.User.users(), totalUsers)
            ),
            entities = users.map { user ->
                SubEntity.EmbeddedRepresentation(
                    rel = listOf(Rels.User.USER, Rels.Collection.ITEM),
                    properties = GetUserOutputModel(user.name, user.email),
                    links = listOf(Links.self(Uris.User.user(user.id).toString())),
                    actions = listOf(Actions.User.getById(user.id))
                )
            }
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