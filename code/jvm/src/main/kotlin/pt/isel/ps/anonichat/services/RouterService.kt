package pt.isel.ps.anonichat.services

import org.springframework.stereotype.Component
import pt.isel.ps.anonichat.domain.certificate.CertificateDomain
import pt.isel.ps.anonichat.domain.router.Router
import pt.isel.ps.anonichat.repository.transaction.TransactionManager
import pt.isel.ps.anonichat.services.models.RouterModel.Companion.toModel
import pt.isel.ps.anonichat.services.models.RoutersModel
import pt.isel.ps.anonichat.services.models.UserModel.Companion.toModel
import pt.isel.ps.anonichat.services.models.UsersModel

@Component
class RouterService(
    private val tm: TransactionManager,
    private val cd: CertificateDomain,
) {
    /**
     * Gets the routers count
     * @param list The list of id
     * @return The list of Routers
     */
    fun getRouters(list: List<Int>): RoutersModel {
        return tm.run { tr ->
            val routers = list.mapNotNull { id ->
                if (tr.routerRepository.isRouter(id)) {
                    tr.routerRepository.getRouterById(id)
                } else null
            }
                .map { router ->
                val cert = if(router.certificate != null) {
                    cd.readFile(router.certificate)
                } else ""
                router.toModel(cert)
            }
            RoutersModel(routers)
        }
    }

    /**
     * Gets the last router id
     * @return The last router id
     */
    fun getLastId(): Int {
        return tm.run {
            it.routerRepository.lastRouterId()
        }
    }

    /**
     * Creates a new router
     * @param ip The ip of the router
     * @param routerCSR The crs of the router
     */
    fun createRouter(ip: String, routerCSR: String, path: String = basePath): Pair<Int, String> {
        return tm.run {
            val id = it.routerRepository.createRouter(ip)
            val certContent = cd.createCertCommand(routerCSR, id, ROUTERS_PASSWORD, path, "", "")
            it.routerRepository.updateCert(id, "$path/$id.crt")

            Pair(id, certContent)
        }
    }

    /**
     * Deletes a router
     */
    fun deleteRouter(id: Int, path: String = basePath): Boolean {
        return tm.run {
            it.routerRepository.deleteRouter(id)
        }
    }

    companion object{
        const val ROUTERS_PASSWORD = "123456789!A"
        private val basePath
            get() = path()
        private fun path() = System.getProperty("user.dir") + "\\certificates\\routers"
    }
}
