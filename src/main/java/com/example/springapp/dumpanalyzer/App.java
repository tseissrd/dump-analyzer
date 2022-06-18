/*
 */
package com.example.springapp.dumpanalyzer;

import com.example.springapp.dumpanalyzer.config.AppConfiguration;
import com.example.springapp.dumpanalyzer.data.FileManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.servlet.function.RequestPredicates.*;
import org.springframework.web.servlet.function.RouterFunction;
import static org.springframework.web.servlet.function.RouterFunctions.*;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 *
 * @author Sovereign
 */
@Configuration
public class App {
  
  @Autowired
  private FileManager fileManager;
  
  @Bean
  public RouterFunction<ServerResponse> getRouters() throws InterruptedException {
    return route()
      .GET("/", accept(ALL),
        request -> ServerResponse.ok()
          .render("index"))
      .GET("/healthcheck", accept(ALL),
        request -> ServerResponse.ok()
          .body("ok"))
      .GET("/status", accept(ALL),
        request -> {
            Map<Object, Object> responseData = new HashMap<>();
            
            return ServerResponse.ok()
              .body(responseData);
          })
      .POST("/list", accept(TEXT_PLAIN),
        request -> {
          String data = request.body(String.class);
          
          if (
              data.equals("")
            )
              return ServerResponse.badRequest()
                .body("Listing root is prohibited");
          
          if (
              data.contains(".")
              || data.contains("/")
              || data.contains("\\")
            )
              return ServerResponse.badRequest()
                .body("Characters \".\", \"/\", \"\\\" are prohibited.");
          
          return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(
                fileManager.list(data)
              );
        })
      .POST("/view", accept(APPLICATION_JSON),
        request -> {
          Map<String, Object> data = request.body(Map.class);
          System.out.println(data);
          
          if (!data.containsKey("type") || !data.containsKey("file"))
            return ServerResponse.badRequest()
              .body("Incorrect request body.");
          
          String type = (String)data.get("type");
          String file = (String)data.get("file");
          
          System.out.println(type + "/" + file);
          
          String contents = fileManager.view(type + "/" + file);
          
          return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(contents);
        })
      .build();
  }
  
}
