package com.z.userservice

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@SpringBootApplication
class UserServiceApplication(private val userService: UserService):ApplicationRunner {
	override fun run(args: ApplicationArguments?) {
		this.userService.save(User(name = "sarasa")).subscribe()
	}
}

fun main(args: Array<String>) {
	runApplication<UserServiceApplication>(*args)
}

@RestController
@RequestMapping("/api/")
class UserController(private val userService: UserService){
	@GetMapping
	fun findAll(): Flux<User>{
		return this.userService.findAll()
	}

	@PostMapping
	fun save(@RequestBody user:User) = this.userService.save(user)
}

@Service
class UserService(private val userDao: UserDao){
	fun findAll() = this.userDao.findAll()
	fun save(user:User) = this.userDao.save(user)
}

interface UserDao:ReactiveMongoRepository<User,String>

@Document
data class User(
	@Id val id:String? = null,
	val name:String
)
