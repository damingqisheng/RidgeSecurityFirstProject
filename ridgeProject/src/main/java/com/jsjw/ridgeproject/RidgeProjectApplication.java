package com.jsjw.ridgeproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class RidgeProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(RidgeProjectApplication.class, args);
    }

}
