/*
 */
package com.example.springapp.dumpanalyzer.data.json;

import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Sovereign
 */
public class JsonOutputStream {
  
  private final OutputStream jsonOutput;
  
  public JsonOutputStream(OutputStream sink) {
    this.jsonOutput = sink;
  }
  
  private String convertObject(Object object) {
    if (Objects.isNull(object))
      return "null";
    else if (object instanceof Number)
      return object.toString();
    else if (
      Boolean.TYPE
        .isInstance(object)
    )
      return String.valueOf((boolean)object);
    else if (object instanceof String)
      return convertString((String)object);
    else if (object instanceof Map)
      return convertMap((Map)object);
    else
      return "\"" + object.toString() + "\"";
  }
  
  private String convertString(String string) {
    return "\"" + string + "\"";
  }
  
  private String convertMap(Map map) {
    if (Objects.isNull(map))
      return "";
    
    StringBuilder result = new StringBuilder("{");
//    map.entrySet()
//      .stream((key, value) -> {
//        
//      })

    return result.toString();
  }
  
  public void writeMap(Map map) {
    
  }
  
  public String test_convertObject(Object object) {
    return convertObject(object);
  }
  
}
