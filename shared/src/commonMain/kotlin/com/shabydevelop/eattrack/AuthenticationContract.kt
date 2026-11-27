package com.shabydevelop.eattrack

fun interface AuthenticationRequest {
    fun cancel()
}

interface IdentityAuthenticator {
    fun authenticate(result: (Boolean) -> Unit): AuthenticationRequest
}
