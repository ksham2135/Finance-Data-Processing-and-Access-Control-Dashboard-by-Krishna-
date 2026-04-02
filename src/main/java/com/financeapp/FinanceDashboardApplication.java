package com.financeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Finance Data Processing & Access Control Dashboard.
 *
 * Spring Boot auto-configures:
 *   - JPA / Hibernate (MySQL)
 *   - Spring Security (JWT)
 *   - SpringDoc OpenAPI (Swagger UI at /api/swagger-ui.html)
 */
@SpringBootApplication
public class FinanceDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceDashboardApplication.class, args);
    }
}
