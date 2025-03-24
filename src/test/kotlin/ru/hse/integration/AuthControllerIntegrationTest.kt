package ru.hse.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import ru.hse.auth.AuthRequest
import ru.hse.auth.AuthResponse
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthControllerIntegrationTest : IntegrationTest() {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `should register user`() {
        val request = AuthRequest(email = "test@example.com", password = "password123")

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val httpEntity = HttpEntity(request, headers)

        val response = restTemplate.postForEntity("/auth/register", request, AuthResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body?.token)
    }

    @Test
    fun `should authenticate registered user`() {
        val email = "authuser@example.com"
        val password = "securepass"

        val registerRequest = AuthRequest(email = email, password = password)
        restTemplate.postForEntity("/auth/register", registerRequest, AuthResponse::class.java)

        val authRequest = AuthRequest(email = email, password = password)
        val response = restTemplate.postForEntity("/auth/login", authRequest, AuthResponse::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body?.token)
    }

}