package http

import Crypto
import com.google.gson.internal.LinkedTreeMap
import domain.Client
import domain.Router
import http.siren.SirenEntity
import http.siren.SubEntity
import java.io.File

/**
 * Extracts the elements from a siren response
 * @param crypto the crypto object
 * @return the list of clients
 */
fun SirenEntity<*>.extractElements(crypto: Crypto): List<*> =
    entities?.map {
        require(it.properties is LinkedTreeMap<*, *>){ "Problem extracting elements from siren response" }
        val id = (it.properties["id"] as Double).toInt()
        val ip = it.properties["ip"] as String
        val certificateContent = it.properties["certificate"] as String
        val certificate = crypto.buildCertificate(certificateContent)
        val name = it.properties["name"] as String?
        if(name != null){
            Client(id, ip, name, certificate)
        }else{
            Router(id, ip, certificate)
        }
    } ?: emptyList<Any>()

/**
 * Extracts the clients from a siren response
 * @param crypto the crypto object
 * @return the list of clients
 */
fun SirenEntity<*>.extractClients(crypto: Crypto): List<Client> =
    entities?.map {
        require(it.properties is LinkedTreeMap<*, *>){ "Problem extracting clients from siren response" }
        val id = it.extractProperty<Double>("id").toInt()
        val name = it.extractProperty<String>("name")
        val ip = it.extractProperty<String>("ip")
        val certificateContent = it.extractProperty<String>("certificate")
        val certificate = crypto.buildCertificate(certificateContent)
        Client(id, ip, name, certificate)
    } ?: emptyList()

/**
 * Extracts the clients from a siren response
 * @param crypto the crypto object
 * @return the list of clients
 */
fun SirenEntity<*>.extractRouters(crypto: Crypto): List<Router> =
    entities?.map {
        require(it.properties is LinkedTreeMap<*, *>){ "Problem extracting clients from siren response" }
        val id = it.extractProperty<Double>("id").toInt()
        val ip = it.extractProperty<String>("ip")
        val certificateContent = it.extractProperty<String>("certificate")
        val certificate = crypto.buildCertificate(certificateContent)
        Router(id, ip, certificate)
    } ?: emptyList()

/**
 * Extracts the property from a siren response
 * @param propertyName the name of the property
 * @return the property as a string
 */
inline fun <reified T> SirenEntity<*>.extractProperty(propertyName: String): T {
    require(properties is LinkedTreeMap<*, *>){ "Problem extracting property from siren response" }
    return properties[propertyName] as T
}

/**
 * Extracts the property from a SubEntity
 * @param propertyName the name of the property
 * @return the property as a string
 */
inline fun <reified T> SubEntity.EmbeddedRepresentation<*>.extractProperty(propertyName: String): T {
    require(properties is LinkedTreeMap<*, *>){ "Problem extracting property from siren response" }
    return properties[propertyName] as T
}