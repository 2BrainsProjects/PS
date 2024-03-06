package pt.isel.ps.anonichat.services.models

data class UsersModel(
    val users: List<UserModel>,
    val maxUsersId: Int
)
