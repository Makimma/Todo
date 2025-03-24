package ru.hse.auth

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import ru.hse.user.User
import ru.hse.user.UserRepository
import kotlin.test.assertEquals
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException

class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var jwtService: JwtService
    private lateinit var authService: AuthService
    private lateinit var authenticationManager: AuthenticationManager

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        passwordEncoder = mockk()
        jwtService = mockk()
        authenticationManager = mockk()

        authService = AuthService(userRepository, passwordEncoder, jwtService, authenticationManager)
    }

    @Test
    fun `should register a new user`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "plainpassword"
        )

        val encodedPassword = "encodedPassword123"
        val savedUser = User(
            id = 1L,
            email = request.email,
            password = encodedPassword,
            name = "default"
        )

        every { userRepository.findByEmail(request.email) } returns null
        every { passwordEncoder.encode(request.password) } returns encodedPassword
        every { userRepository.save(any()) } returns savedUser
        every { jwtService.generateToken(any()) } returns "mock-token"

        val response = authService.register(request)

        assertEquals("mock-token", response.token)
        verify(exactly = 1) { userRepository.save(any()) }
        verify { jwtService.generateToken(any()) }
    }

    @Test
    fun `should authenticate user with correct credentials`() {
        val request = AuthRequest(email = "test@example.com", password = "password123")
        val encodedPassword = "encodedPassword123"
        val user = User(id = 1L, email = request.email, password = encodedPassword)

        every { userRepository.findByEmail(request.email) } returns user

        every { passwordEncoder.matches(request.password, encodedPassword) } returns true

        every { jwtService.generateToken(user) } returns "mocked-jwt-token"

        every {
            authenticationManager.authenticate(any())
        } returns UsernamePasswordAuthenticationToken(user.getUsername(), request.password)

        val response = authService.authenticate(request)

        assertEquals("mocked-jwt-token", response.token)
    }

    @Test
    fun `should throw exception if user not found`() {
        val request = AuthRequest(email = "notfound@example.com", password = "any")

        every { userRepository.findByEmail(request.email) } returns null

        every {
            authenticationManager.authenticate(
                match { it.principal == request.email && it.credentials == request.password }
            )
        } throws UsernameNotFoundException("User not found")

        assertThrows<UsernameNotFoundException> {
            authService.authenticate(request)
        }
    }


    @Test
    fun `should throw exception if password is incorrect`() {
        val request = AuthRequest(email = "test@example.com", password = "wrong")
        val encodedPassword = "encodedPassword"
        val user = User(id = 1L, email = request.email, password = encodedPassword)

        every { userRepository.findByEmail(request.email) } returns user
        every { passwordEncoder.matches(request.password, encodedPassword) } returns false
        every {
            authenticationManager.authenticate(any())
        } throws BadCredentialsException("Incorrect password")

        assertThrows<BadCredentialsException> {
            authService.authenticate(request)
        }
    }

}
