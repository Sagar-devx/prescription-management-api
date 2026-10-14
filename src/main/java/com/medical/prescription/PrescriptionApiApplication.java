package com.medical.prescription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PrescriptionApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrescriptionApiApplication.class, args);
    }
}
