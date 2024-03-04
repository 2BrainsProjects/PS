package pt.isel.ps.anonichat.services.models

import pt.isel.ps.anonichat.domain.user.User

data class UserModel(
    val id: Int,
    val name: String,
    val email: String
) {
    companion object {
        fun User.toModel() = UserModel(id, name, email)
    }
}
