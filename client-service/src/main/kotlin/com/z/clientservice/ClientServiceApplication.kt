package com.z.clientservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
class ClientServiceApplication

fun main(args: Array<String>) {
	runApplication<ClientServiceApplication>(*args)
}

@RestController
class ReservationController(private val helloFeignClient: HelloFeignClient){
	@GetMapping("/feignHi") fun feignHelloWorld() = this.helloFeignClient.helloWorld()
}

@FeignClient(name = "hello-service")
interface HelloFeignClient{
	@GetMapping("/hi") fun helloWorld(): String
}