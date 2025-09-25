package com.evbooking.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EvBookingBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(EvBookingBackendApplication.class, args);
	}

}
