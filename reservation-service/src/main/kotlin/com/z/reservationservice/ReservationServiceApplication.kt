package com.z.reservationservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import reactivefeign.spring.config.EnableReactiveFeignClients
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@SpringBootApplication
@EnableEurekaClient
@EnableReactiveFeignClients
class ReservationServiceApplication{
	@Bean
	@LoadBalanced
	fun loadBalancedWebClientBuilder(): WebClient {
		return WebClient.builder().build()
	}
}

fun main(args: Array<String>) {
	runApplication<ReservationServiceApplication>(*args)
}

@RestController
@RequestMapping("/")
class ReservationController(private  val reservationService: ReservationService, private val webClient: WebClient){
	@GetMapping
	fun findAll() = this.webClient.get().uri("lb://user-service/hi").accept(MediaType.APPLICATION_JSON_UTF8).retrieve().bodyToFlux(String::class.java).subscribe { println(it) }

	@PostMapping
	fun save(@RequestBody reservation: Reservation) = this.reservationService.save(reservation)
}


@Service
class ReservationService(private val reservationReactiveMongoDao: ReservationReactiveMongoDao,
						 private val userReactiveFeignClient: UserReactiveFeignClient) {
		fun findAll() = this.userReactiveFeignClient.findAll()
		fun save(reservation: Reservation): Mono<Reservation> {
				return this.userReactiveFeignClient
					.findById(reservation.user.id)
					.flatMap { user -> reservationReactiveMongoDao.save(reservation.apply { this.user = user }) }

			}
		}


@Repository
interface ReservationReactiveMongoDao: ReactiveCrudRepository<Reservation,String>

@Document
data class Reservation(@Id val id:String? = null, var user: User)


@ReactiveFeignClient(name = "user-service", value = "user-service")
interface UserReactiveFeignClient{
	@GetMapping("/{id}") fun findById(@PathVariable id:String): Mono<User>
	@GetMapping("/") fun findAll(): Flux<User>
}

data class User(val id:String, val name:String)
