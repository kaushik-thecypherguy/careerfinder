package com.acf.careerfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.acf.careerfinder")
public class CareerfinderApplication {
	public static void main(String[] args) {
		SpringApplication.run(CareerfinderApplication.class, args);
	}
}

