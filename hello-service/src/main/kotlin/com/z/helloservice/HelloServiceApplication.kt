package com.z.helloservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class HelloServiceApplication

fun main(args: Array<String>) {
	runApplication<HelloServiceApplication>(*args)
}


@RestController
class UserController(private val helloService: HelloService){
	@GetMapping("/hi") fun helloWorld() = this.helloService.helloWorld()
}

@Service
class HelloService(private val environment: Environment) {
	fun helloWorld() = "Hello from user-service:${environment["local.server.port"]}"
}