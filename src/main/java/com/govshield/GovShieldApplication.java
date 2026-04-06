package com.govshield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.govshield"})
public class GovShieldApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovShieldApplication.class, args);
    }
}
