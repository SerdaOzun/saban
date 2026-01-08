package com.saban.util

import kotlinx.coroutines.Deferred

sealed interface SabanResult {
    data class ErrorResult(val errorMessage: String) : SabanResult
    data class SuccessResult(val successMessage: String) : SabanResult
}

class AuthorizationException(override val message: String) : Exception()
class MissingLanguageException(override val message: String) : Exception()

/**
 * Convenience function to await a Deferred<Result>.
 * If the call to await() throws an error, it wraps the error as a failed Result.
 */
suspend fun <T> Deferred<Result<T>>.awaitResult(): Result<T> {
    return try {
        this.await()
    } catch (e: Throwable) {
        Result.failure(e)
    }
}