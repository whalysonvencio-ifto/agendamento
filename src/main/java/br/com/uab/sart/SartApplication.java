package br.com.uab.sart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SartApplication {

    public static void main(String[] args) {
        SpringApplication.run(SartApplication.class, args);
    }

}
