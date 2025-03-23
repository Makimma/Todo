package ru.hse.task

import jakarta.persistence.*
import ru.hse.user.User
import java.time.LocalDateTime

@Entity
@Table(name = "tasks")
class Task() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var title: String = ""

    var description: String? = null

    var completed: Boolean = false

    var createdAt: LocalDateTime = LocalDateTime.now()

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    fun toResponse(): TaskResponse =
        TaskResponse(
            id = this.id,
            title = this.title,
            description = this.description,
            completed = this.completed,
            createdAt = this.createdAt.toString()
        )
}
