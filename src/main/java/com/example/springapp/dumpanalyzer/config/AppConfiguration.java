package com.example.springapp.dumpanalyzer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 *
 * @author Sovereign
 */
@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class AppConfiguration {
  
  @Autowired
  private Environment env;
  
  public String get(String key) {
    return env.getProperty(key);
  }
  
}
