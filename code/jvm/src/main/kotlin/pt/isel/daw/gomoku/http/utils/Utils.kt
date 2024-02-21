package pt.isel.daw.gomoku.http.utils

import java.net.URI

fun URI.appendQuery(params: Map<String, String>): URI = URI.create(this.toString().addQueryParams(params))

private fun String.addQueryParams(vararg params: Map<String, Any?>): String = params
    .flatMap { it.entries }
    .filter { it.value != null }
    .joinToString("&") { "${it.key}=${it.value}" }
    .let { if (it.isEmpty()) this else "$this?$it" }
