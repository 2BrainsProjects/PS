package http.siren

import Crypto
import com.google.gson.internal.LinkedTreeMap
import domain.Client
import domain.ClientInformation
import domain.Message
import domain.Router

const val SIREN_CLIENTS_PROBLEM_MSG = "Problem extracting clients from siren response"
const val SIREN_PROPERTY_PROBLEM_MSG = "Problem extracting property from siren response"

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
            ClientInformation(id, ip, name, certificate)
        }else{
            Router(id, ip, certificate)
        }
    } ?: emptyList<Any>()

/**
 * Extracts the clients from a siren response
 * @param crypto the crypto object
 * @return the list of clients
 */
fun SirenEntity<*>.extractClient(): Client {
    require(properties is LinkedTreeMap<*, *>){  }
    val id = extractProperty<Double>("id").toInt()
    val name = extractProperty<String>("name")
    return Client(id, name)
}

/**
 * Extracts the clients from a siren response
 * @param crypto the crypto object
 * @return the list of clients
 */
fun SirenEntity<*>.extractClients(crypto: Crypto): List<ClientInformation> =
    entities?.map {
        require(it.properties is LinkedTreeMap<*, *>){ SIREN_CLIENTS_PROBLEM_MSG }
        val id = it.extractProperty<Double>("id").toInt()
        val name = it.extractProperty<String>("name")
        val ip = it.extractProperty<String>("ip")
        val certificateContent = it.extractProperty<String>("certificate")
        val certificate = crypto.buildCertificate(certificateContent)
        ClientInformation(id, ip, name, certificate)
    } ?: emptyList()

/**
 * Extracts the messages from a siren response
 * @return the list of messages

 */
fun SirenEntity<*>.extractMessages() : List<Message> =
    entities?.map {
        require(it.properties is LinkedTreeMap<*, *>){ "Problem extracting messages from siren response" }
        val cid = it.extractProperty<String>("cid")
        val msgDate = it.extractProperty<String>("msgDate")
        val message = it.extractProperty<String>("message")
        Message(cid, message, msgDate)
    } ?: emptyList()

/**
 * Extracts the clients from a siren response
 * @param crypto the crypto object
 * @return the list of clients
 */
fun SirenEntity<*>.extractRouters(crypto: Crypto): List<Router> =
    entities?.map {
        require(it.properties is LinkedTreeMap<*, *>){ SIREN_CLIENTS_PROBLEM_MSG }
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
    require(properties is LinkedTreeMap<*, *>){ SIREN_PROPERTY_PROBLEM_MSG }
    return properties[propertyName] as T
}

/**
 * Extracts the property from a SubEntity
 * @param propertyName the name of the property
 * @return the property as a string
 */
inline fun <reified T> SubEntity.EmbeddedRepresentation<*>.extractProperty(propertyName: String): T {
    require(properties is LinkedTreeMap<*, *>){ SIREN_PROPERTY_PROBLEM_MSG }
    return properties[propertyName] as T
}