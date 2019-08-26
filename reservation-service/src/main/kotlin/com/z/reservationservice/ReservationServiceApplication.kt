package com.z.reservationservice

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.ExceptionHandler
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc
import java.time.LocalDateTime


@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableSwagger2WebMvc
class ReservationServiceApplication{
	@Bean
	fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
			.select()
			.apis(RequestHandlerSelectors.basePackage(ReservationServiceApplication::class.java.`package`.name))
			.paths(PathSelectors.any())
			.build()
}

fun main(args: Array<String>) {
	runApplication<ReservationServiceApplication>(*args)
}

@RestController
@RequestMapping("/api")
class ReservationController(private  val reservationService: ReservationService){
	@GetMapping
	fun findAll() = this.reservationService.findAll()

	@GetMapping("/{id}")
	fun findByUserId(@PathVariable id:String) = this.reservationService.findByUserId(id)

	@GetMapping("hi")
	fun info() = this.reservationService.info()

	@PostMapping
	fun save(@RequestBody reservation: Reservation) = this.reservationService.save(reservation)

	@DeleteMapping("/{id}")
	fun deleteById(@PathVariable id:String) = ResponseEntity.accepted().body(this.reservationService.deleteById(id))
}


@Service
class ReservationService(private val reservationDao: ReservationDao, private val userFeignClient: UserFeignClient) {
	fun info() = this.userFeignClient.info()

	fun findAll(): List<Reservation> = this.reservationDao.findAll()

	fun save(reservation: Reservation): Reservation {
		return this.userFeignClient.findById(reservation.user.id)?.let {
			reservationDao.save(reservation.apply { this.user = it })
		} ?:  throw IllegalArgumentException("User null")
	}

	fun findByUserId(id:String) = this.reservationDao.findByUserId(id)

	fun deleteById(id:String) = this.reservationDao.deleteById(id)
}

@ControllerAdvice
class ExceptionHandler{
	@ExceptionHandler(IllegalArgumentException::class)
	fun handleIllegalArgument(iae: IllegalArgumentException): ResponseEntity<Response> {
		return ResponseEntity.badRequest().body(Response(title = iae::class.java.simpleName,message = iae.localizedMessage))
	}
}

@Repository
interface ReservationDao: MongoRepository<Reservation, String>{
	fun findByUserId(id:String): List<Reservation>
}

@Document
data class Reservation(
		@Id
		@JsonProperty(access = READ_ONLY)
		val id:String? = null,
		var user: User,
		@JsonProperty(access = READ_ONLY)
		val dateTime: LocalDateTime = LocalDateTime.now())

data class Response(val title:String, val message:String, val time:LocalDateTime = LocalDateTime.now())

@FeignClient(name = "user-service")
interface UserFeignClient{
	@GetMapping("/api/{id}") fun findById(@PathVariable id:String): User?
	@GetMapping("/api/") fun findAll(): List<User>
	@GetMapping("/api/hi") fun info(): String
}

data class User(val id:String, val name:String)