package com.example.springapp.cpuloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class SpringBoot extends SpringBootServletInitializer {
  
  @Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SpringBoot.class);
	}

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(SpringBoot.class, args);
	}

}
