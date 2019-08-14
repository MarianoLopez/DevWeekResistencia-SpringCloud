package com.z.userservice

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*


@SpringBootApplication
class UserServiceApplication(private val userService: UserService):ApplicationRunner {
	override fun run(args: ApplicationArguments?) {
		if(this.userService.count() == 0L){
			this.userService.save(User(name = "Mariano"))
		}
	}
}

fun main(args: Array<String>) {
	runApplication<UserServiceApplication>(*args)
}

@RestController
@RequestMapping("/")
class UserController(private val userService: UserService, private val environment: Environment){
	@GetMapping
	fun findAll() = this.userService.findAll()

	@GetMapping("/hi")
	fun hi() = "Hello from user-service:${environment["local.server.port"]}"

	@GetMapping("/{id}")
	fun findById(@PathVariable id:String) = this.userService.findById(id)

	@PostMapping
	fun save(@RequestBody user:User) = this.userService.save(user)
}

@Service
class UserService(private val userReactiveDao: UserReactiveMongoDao) {
	private val logger = LoggerFactory.getLogger(UserService::class.java)
	fun findById(id:String) = this.userReactiveDao.findByIdOrNull(id)

	fun findAll(): List<User> = this.userReactiveDao.findAll().apply {
		logger.info("invoke findAll()")
	}

	fun save(user: User) = this.userReactiveDao.save(user)

	fun count() = this.userReactiveDao.count()
}

@Repository
interface UserReactiveMongoDao: MongoRepository<User, String>

@Document
data class User(@Id val id:String? = null, val name:String)