package br.com.easyhealth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EasyHealthApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyHealthApplication.class, args);
    }
}
