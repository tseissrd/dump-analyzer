/*
 */
package com.example.springapp.dumpanalyzer;

import com.example.springapp.dumpanalyzer.data.ProcessOrchestrator;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.servlet.http.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
  private ProcessOrchestrator orchestrator;
  
  private static final int IO_BUFFER_SIZE = 16384;
  
  private static String readMultipartContent(Part data)
  throws IOException {
    try (Reader reader = new InputStreamReader(
      data.getInputStream(),
      Charset.forName("utf-8")
    )) {
      char[] buf = new char[IO_BUFFER_SIZE / 16];
      
      StringBuilder result = new StringBuilder();
      
      int read;
      
      while ((read = reader.read(buf)) > 0) {
        result.append(buf, 0, read);
      }
      
      return result.toString();
    } catch (IOException ex) {
      throw ex;
    }
  }
  
  @Bean
  public RouterFunction<ServerResponse> getRouters()
  throws InterruptedException {
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
              orchestrator.list(data)
            );
        })
      .POST("/view", accept(APPLICATION_JSON),
        request -> {
          Map data = request.body(Map.class);
          
          if (
            !data.containsKey("type")
            || !data.containsKey("file")
            || !data.containsKey("mode")
          )
            return ServerResponse.badRequest()
              .body("Incorrect request body.");
          
          String type = (String)data.get("type");
          String file = (String)data.get("file");
          String mode = (String)data.get("mode");
          
          return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(
              orchestrator.view(
                file,
                type,
                mode
              )
            );
        })
      .PUT("/upload", accept(MULTIPART_FORM_DATA),
        request -> {
          MultiValueMap data = request.multipartData();
          ArrayList nameDataArray = (ArrayList)data.get("name");
          Part nameData = (Part)nameDataArray.get(0);
          String name = readMultipartContent(nameData);
          
          ArrayList typeDataArray = (ArrayList)data.get("type");
          Part typeData = (Part)typeDataArray.get(0);
          String type = readMultipartContent(typeData);

          ArrayList fileDataArray = (ArrayList)data.get("file");
          if (Objects.isNull(fileDataArray))
            return ServerResponse.badRequest()
              .body("no file");
          
          Part fileData = (Part)fileDataArray.get(0);
          
          orchestrator.accept(
            name,
            type,
            fileData.getInputStream()
          );
          
          return ServerResponse.ok()
            .build();
        })
      .DELETE("/delete", accept(APPLICATION_JSON),
        request -> {
          Map data = (Map)request.body(Map.class);
          
          if (!data.containsKey("type") || !data.containsKey("file"))
            return ServerResponse.badRequest()
              .body("Incorrect request body.");
          
          String type = (String)data.get("type");
          String file = (String)data.get("file");
                    
          orchestrator.remove(file, type);
          
          // JDK 8 FIX
          Map<String, String> response = new HashMap<>();
          response.put("status", "ok");
          
          return ServerResponse.ok()
            .contentType(APPLICATION_JSON)
            .body(
              response
              // Map.of("status", "ok")
            );
        })
      .build();
  }
  
}
