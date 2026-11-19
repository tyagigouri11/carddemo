package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * CardDemo Spring Boot application entry point.
 * Java 17 + Spring Boot 3.x REST API for credit card management.
 * Converted from COBOL using AWS Transform reverse engineering documentation.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.carddemo.repository")
public class CarddemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarddemoApplication.class, args);
    }
}
