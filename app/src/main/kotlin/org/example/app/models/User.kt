package org.example.app.models
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "user", schema = "public") // Maps the Kotlin class to the "user" table
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremented primary key
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
