package pt.isel.ps.anonichat.http.controllers.router

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.ps.anonichat.http.controllers.router.models.GetRouterOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersCountOutputModel
import pt.isel.ps.anonichat.http.media.siren.SirenEntity
import pt.isel.ps.anonichat.http.media.siren.SubEntity
import pt.isel.ps.anonichat.http.utils.Actions
import pt.isel.ps.anonichat.http.utils.Links
import pt.isel.ps.anonichat.http.utils.MediaTypes
import pt.isel.ps.anonichat.http.utils.Params
import pt.isel.ps.anonichat.http.utils.Rels
import pt.isel.ps.anonichat.http.utils.Uris
import pt.isel.ps.anonichat.services.RouterService

@RestController
@RequestMapping(Uris.PREFIX, produces = [MediaTypes.SIREN_MEDIA_TYPE, MediaTypes.PROBLEM_MEDIA_TYPE])
class RouterController (
    val services: RouterService
){
    @GetMapping(Uris.Router.ROUTERS)
    fun getRouters(
        @RequestParam page: Int? = null,
        @RequestParam limit: Int? = null,
        @RequestParam sort: String? = null,
        @RequestParam orderBy: String? = null
    ): ResponseEntity<*> {
        val params = Params(page, limit, sort, orderBy)
        val (routers, totalRouters) = services.getRouters(params.skip, params.limit, params.orderBy, params.sort)
        return SirenEntity(
            clazz = listOf(Rels.Router.ROUTERS, Rels.Collection.COLLECTION),
            properties = GetRoutersCountOutputModel(routers.size),
            links = listOfNotNull(
                Links.self(Uris.Router.ROUTERS),
                params.getPrevPageLink(Uris.Router.routers()),
                params.getNextPageLink(Uris.Router.routers(), totalRouters)
            ),
            entities = routers.map { router ->
                SubEntity.EmbeddedRepresentation(
                    rel = listOf(Rels.Router.ROUTER, Rels.Collection.ITEM),
                    properties = GetRouterOutputModel(router.id, router.ip, router.certificate),
                    links = listOf(Links.self(Uris.Router.router(router.id).toString())),
                    actions = listOf(Actions.Router.getById(router.id))
                )
            }
        ).ok()
    }
}