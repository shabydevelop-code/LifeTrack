package com.shabydevelop.eattrack

internal class RepositoryFailure(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

internal inline fun <T> databaseOperation(action: () -> T): T =
    try { action() } catch (error: android.database.SQLException) {
        throw RepositoryFailure(error.message ?: "Database operation failed", error)
    }
