package ru.hse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class TodoApp

fun main(args: Array<String>) {
    runApplication<TodoApp>(*args)
}