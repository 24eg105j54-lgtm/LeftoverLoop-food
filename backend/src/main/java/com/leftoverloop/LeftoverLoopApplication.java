package com.leftoverloop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LeftoverLoopApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeftoverLoopApplication.class, args);
    }
}
