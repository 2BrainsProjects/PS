package pt.isel.ps.anonichat.http.media.siren

interface Entity<T> {
    val clazz: List<String>?
    val properties: T?
    val entities: List<SubEntity>?
    val actions: List<Action>?
    val links: List<Link>?
}
