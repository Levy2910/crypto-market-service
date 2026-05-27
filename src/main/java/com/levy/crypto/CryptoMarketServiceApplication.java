package com.levy.crypto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoMarketServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoMarketServiceApplication.class, args);
	}

}
