package com.hussain.data_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DataApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(DataApiApplication.class, args);
	}
}