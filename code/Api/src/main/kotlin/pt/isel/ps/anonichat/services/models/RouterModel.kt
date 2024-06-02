package pt.isel.ps.anonichat.services.models

import pt.isel.ps.anonichat.domain.router.Router

data class RouterModel(
    val id: Int,
    val ip: String,
    val certificate: String
) {
    companion object {
        fun Router.toModel(certificate: String) = RouterModel(id, ip, certificate)
    }
}
