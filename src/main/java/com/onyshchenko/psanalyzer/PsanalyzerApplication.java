package com.onyshchenko.psanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PsanalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PsanalyzerApplication.class, args);
	}

}
