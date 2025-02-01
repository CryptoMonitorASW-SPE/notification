@file:Suppress("ktlint:standard:no-wildcard-imports")

package it.unibo.infrastructure.adapter

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

// This service is responsible for validating JWT tokens.
class AuthService(private val jwtSecret: String) {
    /**
     * Validates the JWT token and returns the user ID if valid; otherwise, returns null.
     *
     * @param token The JWT token to validate.
     * @return The userId extracted from the token if valid, or null.
     */
    fun validateToken(token: String): String? {
        return try {
            val algorithm = Algorithm.HMAC256(jwtSecret)
            val verifier = JWT.require(algorithm).build()
            val decodedJWT = verifier.verify(token)
            // Assumes that the token contains a claim named "userId"
            decodedJWT.getClaim("userId").asString()
        } catch (ex: Exception) {
            // Log the exception (in production, consider using a proper logging framework)
            println("JWT validation error: ${ex.message}")
            null
        }
    }
}

/**
 * An extension function on ApplicationCall that authenticates the incoming request.
 *
 * It looks for a JWT token in either a cookie named "authToken" or in the "Authorization" header
 * (in case the token is passed as a Bearer token).
 *
 * @param jwtSecret The secret key used to validate the JWT.
 * @return The userId if the token is valid, or null after sending an unauthorized response.
 */
suspend fun ApplicationCall.authenticate(jwtSecret: String): String? {
    val token =
        request.cookies["authToken"]
            ?: request.headers["Authorization"]
                ?.removePrefix("Bearer ")
                ?.trim()

    if (token == null) {
        respondText("Unauthorized: No auth token provided.", status = HttpStatusCode.Unauthorized)
        return null
    }

    val authService = AuthService(jwtSecret)
    val userId = authService.validateToken(token)
    if (userId == null) {
        respondText("Unauthorized: Invalid auth token.", status = HttpStatusCode.Unauthorized)
        return null
    }
    return userId
}
