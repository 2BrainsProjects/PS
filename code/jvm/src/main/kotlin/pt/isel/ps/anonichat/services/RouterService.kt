package pt.isel.ps.anonichat.services

import org.springframework.stereotype.Component
import pt.isel.ps.anonichat.domain.router.Router
import pt.isel.ps.anonichat.repository.transaction.TransactionManager

@Component
class RouterService(
    private val tm: TransactionManager
) {
    /**
     * Gets the routers count
     * @param list The list of id
     * @return The list of Routers
     */
    fun getRouters(list: List<Int>): List<Router> {
        return tm.run {
            list.mapNotNull { id ->
                if (it.routerRepository.isRouter(id)) {
                    it.routerRepository.getRouterById(id)
                } else null
            }
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
}
