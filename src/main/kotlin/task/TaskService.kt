package ru.hse.task

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.hse.user.User
import ru.hse.user.UserRepository
import java.time.LocalDateTime

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {

    private fun getCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication.name
        return userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $email")
    }

    fun getAllTasksForCurrentUser(): List<TaskResponse> {
        val user = getCurrentUser()
        return taskRepository.findAllByUserId(user.id).map { task ->
            task.toResponse()
        }
    }

    fun createTask(request: TaskRequest): TaskResponse {
        val task = Task().apply {
            title = request.title
            description = request.description
            user = getCurrentUser()
        }
        return taskRepository.save(task).toResponse()
    }

    fun updateTask(id: Long, request: TaskRequest): TaskResponse {
        val user = getCurrentUser()
        val task = taskRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Task not found") }

        if (task.user.id != user.id) {
            throw IllegalAccessException("Not your task")
        }

        task.title = request.title
        task.description = request.description
        return taskRepository.save(task).toResponse()
    }

    fun deleteTask(id: Long) {
        val user = getCurrentUser()
        val task = taskRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Task not found") }

        if (task.user.id != user.id) {
            throw IllegalAccessException("Not your task")
        }

        taskRepository.delete(task)
    }
}
