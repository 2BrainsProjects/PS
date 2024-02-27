package pt.isel.daw.gomoku.http.controllers.home

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.daw.gomoku.http.controllers.home.models.AuthorModel
import pt.isel.daw.gomoku.http.controllers.home.models.GetHomeOutputModel
import pt.isel.daw.gomoku.http.media.siren.SirenEntity
import pt.isel.daw.gomoku.http.utils.Actions
import pt.isel.daw.gomoku.http.utils.Links
import pt.isel.daw.gomoku.http.utils.MediaTypes.PROBLEM_MEDIA_TYPE
import pt.isel.daw.gomoku.http.utils.MediaTypes.SIREN_MEDIA_TYPE
import pt.isel.daw.gomoku.http.utils.Rels
import pt.isel.daw.gomoku.http.utils.Uris

@RestController
@RequestMapping(Uris.PREFIX, produces = [SIREN_MEDIA_TYPE, PROBLEM_MEDIA_TYPE])
class HomeController {

    /**
     * Handles the request to get the home
     * @return the response with the home
     */
    @GetMapping(Uris.HOME)
    fun getHome(req: HttpServletRequest) = SirenEntity(
        clazz = listOf(Rels.HOME),
        properties = home,
        links = getHomeLinks(req),
        actions = listOf(
            Actions.User.register(),
            Actions.User.login(),
            Actions.User.getUsers()
        )
    ).ok()

    private fun getHomeLinks(req: HttpServletRequest) =
        req.getHeader("User-Agent")?.let {
            if (it.contains("Android", ignoreCase = true)) {
                androidLinks
            } else {
                null
            }
        } ?: webLinks

    companion object {
        private val authors = listOf(
            AuthorModel(
                name = "Joana Chuço",
                email = "joanachuco@gmail.com",
                github = "https://github.com/49469",
                number = 49469
            ),
            AuthorModel(
                name = "Diogo Almeida",
                email = "diogoalmeida107@hotmail.com",
                github = "https://github.com/wartuga",
                number = 49449
            )
        )

        private val home = GetHomeOutputModel(
            title = "Gomoku",
            version = "0.0.1",
            description = "Anonymous chat between users",
            authors = authors,
            repository = "https://github.com/isel-leic-daw/2023-daw-leic51d-08"
        )
        private val androidLinks = listOf(Links.self(Uris.HOME), Links.userHome())
        private val webLinks = listOf(Links.self(Uris.HOME), Links.userHome(), Links.user())
    }
}
