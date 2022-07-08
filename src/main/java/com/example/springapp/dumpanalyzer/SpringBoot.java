package com.example.springapp.dumpanalyzer;

import java.util.logging.Level;
import java.util.logging.Logger;
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
    try {
      Test.main(args);
      // SpringApplication.run(SpringBoot.class, args);
    } catch (Throwable ex) {
      Logger.getLogger(SpringBoot.class.getName()).log(Level.SEVERE, null, ex);
    }
	}

}
