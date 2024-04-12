package pt.isel.ps.anonichat.http.controllers.router

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.ps.anonichat.http.controllers.router.models.GetRouterOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersCountOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.RegisterInputModel
import pt.isel.ps.anonichat.http.controllers.router.models.RegisterOutputModel
import pt.isel.ps.anonichat.http.media.siren.SirenEntity
import pt.isel.ps.anonichat.http.media.siren.SubEntity
import pt.isel.ps.anonichat.http.utils.Links
import pt.isel.ps.anonichat.http.utils.MediaTypes
import pt.isel.ps.anonichat.http.utils.Rels
import pt.isel.ps.anonichat.http.utils.Uris
import pt.isel.ps.anonichat.services.RouterService

@RestController
@RequestMapping(Uris.PREFIX, produces = [MediaTypes.SIREN_MEDIA_TYPE, MediaTypes.PROBLEM_MEDIA_TYPE])
class RouterController(
    val services: RouterService
) {
    /**
     * Handles the request to register a new router
     * @param body the request body (RegisterInputModel)
     * @return the response with the user's id
     */
    @PostMapping(Uris.Router.REGISTER)
    fun registerRouter(
        /**
         * removed @RequestBody annotation due to spring doesn´t recognize
         * Content-Type: application/x-www-form-urlencoded as a possible body
         */
        body: RegisterInputModel
    ): ResponseEntity<*> {
        println("oioioioioioioioioioioioioioioioioioioioioioioioioioioioioioioioioioioio")
        val routerId = services.createRouter(body.routerCSR, body.pwd)
        return SirenEntity(
            clazz = listOf(Rels.Router.REGISTER),
            properties = RegisterOutputModel(routerId)
        ).created(Uris.User.home())
    }

    /**
     * Handles the request to get list of routers
     * @param ids the request body (GetRoutersCountInputModel)
     * @return the response with the list of routers
     */
    @GetMapping(Uris.Router.ROUTERS)
    fun getRouters(
        @Valid @RequestParam
        ids: List<Int>
    ): ResponseEntity<*> {
        val (routers) = services.getRouters(ids)
        return SirenEntity(
            clazz = listOf(Rels.Router.ROUTERS, Rels.Collection.COLLECTION),
            properties = GetRoutersOutputModel(routers.size),
            links = listOfNotNull(
                Links.self(Uris.Router.ROUTERS)
            ),
            entities = routers.map { router ->
                SubEntity.EmbeddedRepresentation(
                    rel = listOf(Rels.Collection.ITEM),
                    properties = GetRouterOutputModel(router.id, router.ip, router.certificate)
                )
            }
        ).ok()
    }

    /**
     * Handles the request to get the max routers id
     * @return the response with the routers max id
     */
    @GetMapping(Uris.Router.ROUTERS_COUNT)
    fun getRoutersCount(): ResponseEntity<*> {
        val id = services.getLastId()
        return SirenEntity(
            clazz = listOf(Rels.Router.ROUTERS_COUNT),
            properties = GetRoutersCountOutputModel(id),
            links = listOfNotNull(
                Links.self(Uris.Router.ROUTERS_COUNT)
            )
        ).ok()
    }

    @DeleteMapping(Uris.Router.DELETE)
    fun deleteRouter(
        @Valid @PathVariable
        id: Int,
        @Valid @RequestParam
        pwd: String
    ): ResponseEntity<*> {
        services.deleteRouter(id, pwd)
        return SirenEntity<Unit>(
            clazz = listOf(Rels.Router.DELETE)
        ).ok()
    }
}
