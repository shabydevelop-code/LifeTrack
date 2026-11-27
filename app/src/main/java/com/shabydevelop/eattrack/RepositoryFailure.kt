package com.shabydevelop.eattrack

internal inline fun <T> databaseOperation(action: () -> T): T =
    try { action() } catch (error: android.database.SQLException) {
        throw RepositoryFailure(error.message ?: "Database operation failed", error)
    }
