package ru.hse.auth

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.hse.user.User
import ru.hse.user.UserRepository

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {

    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("Email already registered")
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        userRepository.save(user)
        val jwtToken = jwtService.generateToken(user)
        return AuthResponse(token = jwtToken)
    }

    fun authenticate(request: AuthRequest): AuthResponse {
        val authToken = UsernamePasswordAuthenticationToken(
            request.email,
            request.password
        )
        authenticationManager.authenticate(authToken)

        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("User not found")

        val jwtToken = jwtService.generateToken(user)
        return AuthResponse(token = jwtToken)
    }
}