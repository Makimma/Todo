package ru.hse.user

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
data class User(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,

        @Column(unique = true, nullable = false)
        private val email: String = "",

        @Column(nullable = false)
        private val password: String = "",

        //new field for migration test (V2__add_index_email.sql)
        @Column(nullable = false)
        val name: String = "test_name"

) : UserDetails {

        override fun getUsername(): String = email
        override fun getPassword(): String = password

        override fun getAuthorities() = emptyList<GrantedAuthority>()
        override fun isAccountNonExpired() = true
        override fun isAccountNonLocked() = true
        override fun isCredentialsNonExpired() = true
        override fun isEnabled() = true
}