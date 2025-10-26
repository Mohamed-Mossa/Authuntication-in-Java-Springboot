package com.erp.valid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ValidApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValidApplication.class, args);
	}

}
