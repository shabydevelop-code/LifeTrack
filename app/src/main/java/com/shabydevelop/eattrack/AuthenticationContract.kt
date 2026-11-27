package com.shabydevelop.eattrack

internal fun interface AuthenticationRequest {
    fun cancel()
}

internal interface IdentityAuthenticator {
    fun authenticate(result: (Boolean) -> Unit): AuthenticationRequest
}
