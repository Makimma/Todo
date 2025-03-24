package ru.hse.integration

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import ru.hse.auth.AuthRequest
import ru.hse.auth.AuthResponse
import ru.hse.task.TaskRequest
import ru.hse.task.TaskResponse
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TaskControllerIntegrationTest : IntegrationTest() {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `should create task for authenticated user`() {
        val email = "taskuser@example.com"
        val password = "taskpass"

        val registerRequest = AuthRequest(email = email, password = password)
        restTemplate.postForEntity("/auth/register", registerRequest, AuthResponse::class.java)

        val authResponse = restTemplate.postForEntity("/auth/login", registerRequest, AuthResponse::class.java)
        val token = authResponse.body?.token
        assertNotNull(token, "JWT token должен быть получен после авторизации")

        val taskRequest = TaskRequest(title = "My Task", description = "Test task")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(token)

        val taskResponse = restTemplate.postForEntity(
            "/tasks",
            HttpEntity(taskRequest, headers),
            TaskResponse::class.java
        )

        assertEquals(HttpStatus.CREATED, taskResponse.statusCode)
        assertEquals("My Task", taskResponse.body?.title)
        assertEquals("Test task", taskResponse.body?.description)
    }

    @Test
    fun `should return all tasks for authenticated user`() {
        val email = "getuser@example.com"
        val password = "getpass"

        val registerRequest = AuthRequest(email = email, password = password)
        restTemplate.postForEntity("/auth/register", registerRequest, AuthResponse::class.java)
        val authResponse = restTemplate.postForEntity("/auth/login", registerRequest, AuthResponse::class.java)
        val token = authResponse.body?.token
        assertNotNull(token, "JWT token должен быть получен")

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(token)
        }

        val task1 = TaskRequest(title = "Task 1", description = "Desc 1")
        val task2 = TaskRequest(title = "Task 2", description = "Desc 2")

        restTemplate.postForEntity("/tasks", HttpEntity(task1, headers), TaskResponse::class.java)
        restTemplate.postForEntity("/tasks", HttpEntity(task2, headers), TaskResponse::class.java)

        val response = restTemplate.exchange(
            "/tasks",
            HttpMethod.GET,
            HttpEntity(null, headers),
            Array<TaskResponse>::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        val tasks = response.body
        assertNotNull(tasks)
        assertEquals(2, tasks.size)
        assertEquals("Task 1", tasks[0].title)
        assertEquals("Task 2", tasks[1].title)
    }

    @Test
    fun `should update task for authenticated user`() {
        val email = "updateuser@example.com"
        val password = "updatepass"

        val registerRequest = AuthRequest(email = email, password = password)
        restTemplate.postForEntity("/auth/register", registerRequest, AuthResponse::class.java)
        val authResponse = restTemplate.postForEntity("/auth/login", registerRequest, AuthResponse::class.java)
        val token = authResponse.body?.token
        assertNotNull(token, "JWT token должен быть получен")

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(token)
        }

        val createRequest = TaskRequest(title = "Initial Title", description = "Initial Desc")
        val createResponse = restTemplate.postForEntity(
            "/tasks",
            HttpEntity(createRequest, headers),
            TaskResponse::class.java
        )
        val taskId = createResponse.body?.id
        assertNotNull(taskId, "Задача должна быть создана")

        val updateRequest = TaskRequest(title = "Updated Title", description = "Updated Desc")
        val updateResponse = restTemplate.exchange(
            "/tasks/$taskId",
            HttpMethod.PUT,
            HttpEntity(updateRequest, headers),
            TaskResponse::class.java
        )

        assertEquals(HttpStatus.OK, updateResponse.statusCode)
        val updatedTask = updateResponse.body
        assertNotNull(updatedTask)
        assertEquals(taskId, updatedTask.id)
        assertEquals("Updated Title", updatedTask.title)
        assertEquals("Updated Desc", updatedTask.description)
    }

    @Test
    fun `should delete task for authenticated user`() {
        val email = "deleteuser@example.com"
        val password = "deletepass"

        val registerRequest = AuthRequest(email = email, password = password)
        restTemplate.postForEntity("/auth/register", registerRequest, AuthResponse::class.java)
        val authResponse = restTemplate.postForEntity("/auth/login", registerRequest, AuthResponse::class.java)
        val token = authResponse.body?.token
        assertNotNull(token)

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(token)
        }

        val taskRequest = TaskRequest(title = "To Be Deleted", description = "Will be gone soon")
        val createResponse = restTemplate.postForEntity(
            "/tasks",
            HttpEntity(taskRequest, headers),
            TaskResponse::class.java
        )
        val taskId = createResponse.body?.id
        assertNotNull(taskId)

        val deleteResponse = restTemplate.exchange(
            "/tasks/$taskId",
            HttpMethod.DELETE,
            HttpEntity<Void>(headers),
            Void::class.java
        )
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.statusCode)

        val tasksResponse = restTemplate.exchange(
            "/tasks",
            HttpMethod.GET,
            HttpEntity<Void>(headers),
            Array<TaskResponse>::class.java
        )

        val remainingTasks = tasksResponse.body ?: emptyArray()
        Assertions.assertTrue(remainingTasks.none { it.id == taskId })
    }

}
