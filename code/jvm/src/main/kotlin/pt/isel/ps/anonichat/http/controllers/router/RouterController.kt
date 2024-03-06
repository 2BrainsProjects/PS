package pt.isel.ps.anonichat.http.controllers.router

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.ps.anonichat.http.controllers.router.models.GetRouterOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersCountInputModel
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersCountOutputModel
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
     * Handles the request to get list of routers
     * @param body the request body (GetRoutersCountInputModel)
     * @return the response with the list of routers
     */
    @GetMapping(Uris.Router.ROUTERS)
    fun getRouters(
        @Valid @RequestBody
        body: GetRoutersCountInputModel
    ): ResponseEntity<*> {
        val routers = services.getRouters(body.routersIdList)
        return SirenEntity(
            clazz = listOf(Rels.Router.ROUTERS, Rels.Collection.COLLECTION),
            properties = GetRoutersCountOutputModel(routers.size),
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
}
