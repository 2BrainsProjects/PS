package http

/**
 * Extracts the properties string from the body
 */
fun getPropertiesString(body: String) = body.split("properties")

/**
 * Extracts the properties string from the body where it has the id
 */
fun getPropertiesStringsFromId(body: String) = getPropertiesString(body).filter { it.contains("id") }

/**
 * Extracts the first properties string from the body where it has the id
 */
fun getFirstPropertiesStringFromId(body: String): String = getPropertiesStringsFromId(body).first()

/**
 * Extracts the id from the properties string
 */
fun getId(body: String): Int? = body.split(',').firstOrNull { it.contains("id") }?.split(":")?.last()?.toIntOrNull()

/**
 * Extracts the ip from the properties string
 */
fun getCertificate(body: String): String = getParam(body, "certificate").dropWhile { it != '-' }.dropLastWhile { it != '-' }

/**
 * Extracts the name from the properties string
 */
fun getName(body: String): String = getParam(body, "name")

fun getMaxId(body: String): String = getParam(body, "maxId")

private fun getParam(
    body: String,
    paramName: String,
): String = body.split(',').first { it.contains(paramName) }
