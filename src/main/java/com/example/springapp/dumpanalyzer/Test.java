/*
 */
package com.example.springapp.dumpanalyzer;

import com.example.springapp.dumpanalyzer.data.json.JsonOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sovereign
 */
public class Test {
  
  public static void main(String[] args) throws Throwable {
    System.out.println("in test");
    JsonOutputStream jsonOut = new JsonOutputStream(
      System.out
    );
    Map<String, Object> test = new HashMap<>();
    test.put("myKey", 123);
    test.put("key2", (double)543.123);

    jsonOut.write(test);
    jsonOut.close();
    
    System.out.println("exit test");
  }
  
}
