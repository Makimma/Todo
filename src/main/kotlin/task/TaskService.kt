package ru.hse.task

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.hse.user.User
import ru.hse.user.UserRepository
import org.slf4j.LoggerFactory

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(TaskService::class.java)

    private fun getCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication.name
        return userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $email")
    }

    fun getAllTasksForCurrentUser(): List<TaskResponse> {
        val user = getCurrentUser()
        logger.info("Fetching tasks for user: ${user.getUsername()}")
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
        logger.info("Creating task for user: ${getCurrentUser().getUsername()} with title: ${request.title}")
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
        logger.info("Updating task $id for user: ${user.getUsername()}")
        return taskRepository.save(task).toResponse()
    }

    fun deleteTask(id: Long) {
        val user = getCurrentUser()
        val task = taskRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Task not found") }

        if (task.user.id != user.id) {
            throw IllegalAccessException("Not your task")
        }
        logger.info("Deleting task $id for user: ${user.getUsername()}")
        taskRepository.delete(task)
    }
}
