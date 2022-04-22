/*
 */
package com.example.springapp.cpuloader;

import com.example.springapp.cpuloader.threads.ThreadDestroyer;
import com.example.springapp.cpuloader.threads.ThreadDestroyer.Strategy;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.http.MediaType.*;
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
  private ThreadDestroyer threadDestroyer;
  
  public void controlThreadDestroyer(Strategy strategy, int threadsNumber) {
    Logger.getLogger(
      App.class.getName()
    ).log(
      Level.INFO,
      "There are {0} thread(s) now, strategy is {1}...",
      new Object[]{threadsNumber, strategy.toString()}
    );
    
    threadDestroyer.setStrategy(strategy);
    threadDestroyer.load(threadsNumber);
  }
  
  @Bean
  public RouterFunction<ServerResponse> getRouters() throws InterruptedException {
    return route()
      .GET("/", accept(ALL),
        request -> ServerResponse.ok()
          .render("index"))
      .GET("/healthcheck", accept(ALL),
        request -> ServerResponse.ok()
          .body("ok"))
      .POST("/go", accept(APPLICATION_JSON),
        request -> {
            Map<String, Object> data = request.body(Map.class);
            if (data == null)
              return ServerResponse.badRequest()
                .body("missing parameters or incorrect format");
                    
            int threadsNumber = (int)data.get("threadsNumber");
            String strategyString = (String)data.get("strategy");
            Strategy strategy = ThreadDestroyer.Strategy
              .valueOf(strategyString);

            controlThreadDestroyer(strategy, threadsNumber);
            return ServerResponse.ok()
              .body("ok");
          })
      .GET("/status", accept(ALL),
        request -> {
            Map<String, Object> responseData = new HashMap<>();
            
            responseData.put(
                "strategy",
                threadDestroyer
                  .getStrategy()
                  .toString()
              );
            
            responseData.put(
                "threadsNumber",
                threadDestroyer
                  .getThreadsNumber()
              );
          
            return ServerResponse.ok()
              .body(responseData);
          })
      .build();
  }
  
}
