package com.z.reservationservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime


@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
class ReservationServiceApplication

fun main(args: Array<String>) {
	runApplication<ReservationServiceApplication>(*args)
}

@RestController
@RequestMapping("/")
class ReservationController(private  val reservationService: ReservationService){
	@GetMapping
	fun findAll() = this.reservationService.findAll()
	@GetMapping("hi") fun info() = this.reservationService.info()
	@PostMapping
	fun save(@RequestBody reservation: Reservation) = this.reservationService.save(reservation)
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
		}

@ControllerAdvice
class ExceptionHandler{
	@ExceptionHandler(IllegalArgumentException::class)
	fun handleIllegalArgument(iae: IllegalArgumentException): ResponseEntity<Response> {
		return ResponseEntity.badRequest().body(Response(title = iae::class.java.simpleName,message = iae.localizedMessage))
	}
}

@Repository
interface ReservationDao: MongoRepository<Reservation, String>

@Document
data class Reservation(@Id val id:String? = null, var user: User, val dateTime: LocalDateTime = LocalDateTime.now())

data class Response(val title:String, val message:String, val time:LocalDateTime = LocalDateTime.now())

@FeignClient(name = "user-service")
interface UserFeignClient{
	@GetMapping("/{id}") fun findById(@PathVariable id:String): User?
	@GetMapping("/") fun findAll(): List<User>
	@GetMapping("/hi") fun info(): String
}

data class User(val id:String, val name:String)
