package pt.isel.ps.anonichat.services

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.isel.ps.anonichat.domain.certificate.CertificateDomain
import pt.isel.ps.anonichat.domain.exceptions.RouterException.InvalidCredentialsException
import pt.isel.ps.anonichat.domain.exceptions.requireOrThrow
import pt.isel.ps.anonichat.domain.utils.readFile
import pt.isel.ps.anonichat.repository.transaction.TransactionManager
import pt.isel.ps.anonichat.services.models.RouterModel.Companion.toModel
import pt.isel.ps.anonichat.services.models.RoutersModel
import java.io.File

@Component
class RouterService(
    private val passwordEncoder: PasswordEncoder,
    private val tm: TransactionManager,
    private val cd: CertificateDomain
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
                } else {
                    null
                }
            }
                .map { router ->
                    val cert = if (router.certificate != null) {
                        readFile(router.certificate)
                    } else {
                        ""
                    }
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
     * @param path The path of certificate
     * @return The router's id
     */
    fun createRouter(ip: String, routerCSR: String, pwd: String, path: String = basePath): Int {
        return tm.run {
            // Hash the password
            val passwordHash = passwordEncoder.encode(pwd)

            // Create the router
            val id = it.routerRepository.createRouter(passwordHash)

            // Certificate need the router's id to be created
            val certPath = cd.createCertCommand(routerCSR, id, path)

            // Update the router with the certificate path and ip
            it.routerRepository.updateRouter(id, ip, certPath)

            id
        }
    }

    /**
     * Deletes a router
     * @param id the router's id
     * @param path The path of certificate
     * @return If the router was deleted with success
     */
    fun deleteRouter(id: Int, pwd: String, path: String = basePath): Boolean {
        return tm.run {
            val hashedPassword = it.routerRepository.getRouterById(id).passwordHash
            requireOrThrow<InvalidCredentialsException>(passwordEncoder.matches(pwd, hashedPassword)) {
                "Incorrect password"
            }
            val file = File("$path\\$id.cer")
            file.delete()
            it.routerRepository.deleteRouter(id)
        }
    }

    companion object {
        private val basePath
            get() = path()
        private fun path() = System.getProperty("user.dir") + "\\certificates\\routers"
    }
}
