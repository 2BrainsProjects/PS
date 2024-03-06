package pt.isel.ps.anonichat.services.models

import pt.isel.ps.anonichat.domain.user.User

data class UserModel(
    val id: Int,
    val ip: String,
    val name: String,
    val email: String,
    val certificate: String
) {
    companion object {
        fun User.toModel() = UserModel(id, ip, name, email, certificate)
    }
}
