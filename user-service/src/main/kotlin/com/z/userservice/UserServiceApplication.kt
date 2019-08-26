package com.z.userservice

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc


@SpringBootApplication
@EnableSwagger2WebMvc
class UserServiceApplication(private val userService: UserService):ApplicationRunner {
	override fun run(args: ApplicationArguments?) {
		if(this.userService.count() == 0L){
			this.userService.save(User(name = "Mariano"))
		}
	}

	@Bean
	fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
			.select()
			.apis(RequestHandlerSelectors.basePackage(UserServiceApplication::class.java.`package`.name))
			.paths(PathSelectors.any())
			.build()
}

fun main(args: Array<String>) {
	runApplication<UserServiceApplication>(*args)
}

@RestController
@RequestMapping("/api")
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
data class User(@Id @JsonProperty(access = READ_ONLY) val id:String? = null, val name:String)