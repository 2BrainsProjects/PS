package pt.isel.ps.anonichat.domain.exceptions

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Requires that the given [value] is `true` or throws an exception to the type [T] with the given [lazyMessage]
 * @param T the type of the exception to throw
 * @param value the value to check
 * @param lazyMessage the message to be used in the exception if the [value] is `false`
 * @throws T if the [value] is `false`
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T : Exception> requireOrThrow(value: Boolean, lazyMessage: () -> Any) {
    contract {
        returns() implies value
    }
    if (!value) {
        val message = lazyMessage().toString()
        throw T::class.constructors.first().call(message)
    }
}
