package it.unibo.application

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import it.unibo.infrastructure.adapter.AuthAdapter
import java.util.Date
import kotlin.test.Test

class AuthenticationTest {
    private val secretTestToken = "4c86564757d1f71d97fbddd88f614c9e5ea0847d3a8df1631bb501ab66599d76"

    private fun createJwtToken(): String {
        // Define the algorithm with your secret key
        val algorithm = Algorithm.HMAC256(secretTestToken)

        // Set token expiration time (1 hour from now)
        val expirationTime = Date(System.currentTimeMillis() + 3600 * 1000)

        // Create the token
        return JWT.create()
            .withClaim("userId", "test_user")
            .withExpiresAt(expirationTime)
            .sign(algorithm)
    }

    @Test
    fun `test validateToken with valid token`() {
        val tokenCreated = createJwtToken()
        val authAdapter = AuthAdapter(secretTestToken)
        val userId = authAdapter.validateToken(tokenCreated)
        assert(userId != null)
        assert(userId == "test_user")
    }

    @Test
    fun `test validateToken with malformed token`() {
        val malformedToken = "this.is.not.a.valid.token"
        val authAdapter = AuthAdapter(secretTestToken)
        val userId = authAdapter.validateToken(malformedToken)
        assert(userId == null) { "Expected null for malformed token, but got $userId" }
    }

    @Test
    fun `test validateToken with token signed by different secret`() {
        // Create a token with a different secret
        val differentSecret = "a_different_secret_key"
        val algorithm = Algorithm.HMAC256(differentSecret)
        val expirationTime = Date(System.currentTimeMillis() + 3600 * 1000)
        val token =
            JWT.create()
                .withClaim("userId", "test_user")
                .withExpiresAt(expirationTime)
                .sign(algorithm)

        val authAdapter = AuthAdapter(secretTestToken)
        val userId = authAdapter.validateToken(token)
        assert(userId == null) { "Expected null for token signed with different secret, but got $userId" }
    }

    @Test
    fun `test validateToken with expired token`() {
        // Create a token that expired 1 hour ago
        val algorithm = Algorithm.HMAC256(secretTestToken)
        val expirationTime = Date(System.currentTimeMillis() - 3600 * 1000)
        val token =
            JWT.create()
                .withClaim("userId", "test_user")
                .withExpiresAt(expirationTime)
                .sign(algorithm)

        val authAdapter = AuthAdapter(secretTestToken)
        val userId = authAdapter.validateToken(token)
        assert(userId == null) { "Expected null for expired token, but got $userId" }
    }

    @Test
    fun `test validateToken with token missing userId claim`() {
        // Create a token without the userId claim
        val algorithm = Algorithm.HMAC256(secretTestToken)
        val expirationTime = Date(System.currentTimeMillis() + 3600 * 1000)
        val token =
            JWT.create()
                .withExpiresAt(expirationTime)
                .sign(algorithm)

        val authAdapter = AuthAdapter(secretTestToken)
        val userId = authAdapter.validateToken(token)
        assert(userId == null) { "Expected null for token missing userId claim, but got $userId" }
    }
}
