package ru.hse.task

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import ru.hse.user.User
import ru.hse.user.UserRepository
import java.time.LocalDateTime
import java.util.*

class TaskServiceTest {

    private val taskRepository: TaskRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk()
    private val taskService = TaskService(taskRepository, userRepository)

    @Test
    fun `should create task for current user`() {
        val request = TaskRequest(title = "Test Task", description = "Task description")
        val user = User(id = 1L, email = "test@example.com", password = "pass", name = "User")

        val authentication = mockk<Authentication>()
        every { authentication.name } returns user.username

        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)

        every { userRepository.findByEmail(user.username) } returns user

        val taskSlot = slot<Task>()
        every { taskRepository.save(capture(taskSlot)) } answers {
            taskSlot.captured.apply {
                id = 10L
                createdAt = LocalDateTime.now()
            }
        }

        val response = taskService.createTask(request)

        assertEquals("Test Task", response.title)
        assertEquals("Task description", response.description)
        assertEquals(10L, response.id)
        verify { taskRepository.save(any()) }

        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should return all tasks for current user`() {
        val user = User(id = 1, email = "test@example.com", password = "securepass")
        val tasks = listOf(
            Task().apply {
                id = 1
                title = "Task 1"
                description = "Desc 1"
                this.user = user
            },
            Task().apply {
                id = 2
                title = "Task 2"
                description = "Desc 2"
                this.user = user
            }
        )


        every { userRepository.findByEmail(user.getUsername()) } returns user
        mockkStatic(SecurityContextHolder::class)
        val auth = mockk<Authentication>()
        every { auth.name } returns user.getUsername()
        every { SecurityContextHolder.getContext().authentication } returns auth
        every { taskRepository.findAllByUserId(user.id) } returns tasks

        val result = taskService.getAllTasksForCurrentUser()

        assertEquals(2, result.size)
        assertEquals("Task 1", result[0].title)
        assertEquals("Task 2", result[1].title)
    }

    @Test
    fun `should update task for current user`() {
        val email = "test@example.com"
        val user = User(id = 1, email = email, password = "encoded")
        val request = TaskRequest(title = "Updated Title", description = "Updated Description")
        val existingTask = Task().apply {
            id = 1
            title = "Old Title"
            description = "Old Desc"
            this.user = user
        }

        val auth = UsernamePasswordAuthenticationToken(email, null)
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = auth
        SecurityContextHolder.setContext(context)

        every { userRepository.findByEmail(email) } returns user
        every { taskRepository.findById(1) } returns Optional.of(existingTask)
        every { taskRepository.save(any()) } answers { firstArg() }

        val result = taskService.updateTask(1, request)

        assertEquals(request.title, result.title)
        assertEquals(request.description, result.description)
        verify { taskRepository.save(match { it.id == 1L && it.title == request.title }) }

        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should delete task for current user`() {
        val email = "test@example.com"
        val user = User(id = 1, email = email, password = "encoded")

        val taskToDelete = Task().apply {
            id = 1
            title = "To delete"
            description = "Will be deleted"
            this.user = user
        }

        val auth = UsernamePasswordAuthenticationToken(email, null)
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = auth
        SecurityContextHolder.setContext(context)

        every { userRepository.findByEmail(email) } returns user
        every { taskRepository.findById(1) } returns Optional.of(taskToDelete)
        every { taskRepository.delete(any()) } just Runs

        taskService.deleteTask(1)

        verify { taskRepository.delete(taskToDelete) }

        SecurityContextHolder.clearContext()
    }

}
