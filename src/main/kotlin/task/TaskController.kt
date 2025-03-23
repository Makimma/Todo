package ru.hse.task

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping
    fun getTasks(): List<TaskResponse> =
        taskService.getAllTasksForCurrentUser()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createTask(@RequestBody request: TaskRequest): TaskResponse =
        taskService.createTask(request)

    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: Long,
        @RequestBody request: TaskRequest
    ): TaskResponse =
        taskService.updateTask(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTask(@PathVariable id: Long) =
        taskService.deleteTask(id)
}
