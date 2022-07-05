/*
 */
package com.example.springapp.dumpanalyzer;

import com.example.springapp.dumpanalyzer.data.FileManager;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.jetty.util.MultiPartInputStreamParser.MultiPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import static org.springframework.http.MediaType.*;
import org.springframework.util.MultiValueMap;
import static org.springframework.web.servlet.function.RequestPredicates.*;
import org.springframework.web.servlet.function.RouterFunction;
import static org.springframework.web.servlet.function.RouterFunctions.*;
import org.springframework.web.servlet.function.ServerResponse;

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
      .POST("/upload", accept(MULTIPART_FORM_DATA),
        request -> {
          MultiValueMap data = request.multipartData();
          ArrayList nameDataArray = (ArrayList)data.get("name");
          MultiPart nameData = (MultiPart)nameDataArray.get(0);
          String name = new String(nameData.getBytes(), "utf-8");
          System.out.println(data.get("file").getClass());
          ArrayList fileDataArray = (ArrayList)data.get("file");
          if (Objects.isNull(fileDataArray))
            return ServerResponse.badRequest()
              .body("no file");
          
          System.out.println(name);
          System.out.println(fileDataArray.size());
          System.out.println(fileDataArray.get(0).getClass());
          MultiPart fileData = (MultiPart)fileDataArray.get(0);
          // String fileName = fileData.getSubmittedFileName();
          File file = fileData.getFile();
          // System.out.println(fileName);
          System.out.println(file);
          System.out.println(fileData.getInputStream());
          fileManager.accept(
            name,
            fileData.getInputStream()
          );
          
          return ServerResponse.ok()
            .build();
        })
      .build();
  }
  
}
