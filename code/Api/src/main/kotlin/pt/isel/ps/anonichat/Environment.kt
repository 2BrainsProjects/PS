package pt.isel.ps.anonichat

object Environment {
    private const val DATABASE_URL = "DATABASE_URL"

    fun getDbUrl() = System.getenv(DATABASE_URL) ?: throw Exception("Missing environment variable '$DATABASE_URL'")
}
