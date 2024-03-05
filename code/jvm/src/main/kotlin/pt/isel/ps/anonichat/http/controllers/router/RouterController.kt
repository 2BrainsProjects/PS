package pt.isel.ps.anonichat.http.controllers.router

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.ps.anonichat.http.controllers.router.models.GetRouterOutputModel
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersCountInputModel
import pt.isel.ps.anonichat.http.controllers.router.models.GetRoutersCountOutputModel
import pt.isel.ps.anonichat.http.media.siren.SirenEntity
import pt.isel.ps.anonichat.http.media.siren.SubEntity
import pt.isel.ps.anonichat.http.utils.Actions
import pt.isel.ps.anonichat.http.utils.Links
import pt.isel.ps.anonichat.http.utils.MediaTypes
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
        @Valid @RequestBody
        body: GetRoutersCountInputModel,
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
                    rel = listOf(Rels.Router.ROUTER, Rels.Collection.ITEM),
                    properties = GetRouterOutputModel(router.id, router.ip, router.certificate),
                    links = listOf(Links.self(Uris.Router.router(router.id).toString())),
                    actions = listOf(Actions.Router.getById(router.id))
                )
            }
        ).ok()
    }

    @GetMapping(Uris.Router.ROUTERS_COUNT)
    fun getRoutersCount( ): ResponseEntity<*> {
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