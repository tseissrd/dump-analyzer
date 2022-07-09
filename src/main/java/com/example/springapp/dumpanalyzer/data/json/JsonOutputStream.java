/*
 */
package com.example.springapp.dumpanalyzer.data.json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import static java.util.Objects.requireNonNull;

/**
 *
 * @author Sovereign
 */
public class JsonOutputStream {
  
  private final BufferedWriter jsonOutput;
  
  public JsonOutputStream(OutputStream sink) {
    this.jsonOutput = new BufferedWriter(
      new OutputStreamWriter(sink)
    );
  }
  
  public void write(Object object)
  throws IOException {
    if (Objects.isNull(object))
      jsonOutput.write("null");
    else if (object instanceof Number)
      jsonOutput.write(object.toString());
    else if (
      Boolean.TYPE
        .isInstance(object)
    ) {
      jsonOutput.write(
        String.valueOf(
          (boolean)object
        )
      );
      return;
    } else if (object instanceof String) {
      writeString((String)object);
      return;
    } else if (object instanceof Map) {
      writeMap((Map)object);
      return;
    } else {
      jsonOutput.write(
        new StringBuilder("\"")
          .append(object.toString())
          .append("\"")
          .toString()
      );
      return;
    }
  }
  
  public void writeString(String string)
  throws IOException {
    jsonOutput.write(
      new StringBuilder("\"")
        .append(string)
        .append("\"")
        .toString()
    );
  }
  
  private void writeMapEntry(Entry<String, Object> entry)
  throws IOException {
    jsonOutput.write(
      new StringBuilder("\"")
        .append(entry.getKey())
        .append("\":")
        .toString()
    );
    write(entry.getValue());
  }
  
  public void writeMap(Map<String, Object> map)
  throws IOException {
    if (Objects.isNull(map))
      return;
      // return "";
    
    jsonOutput.write("{");
    
    Entry<String, Object> lastEntry = null;
    
    for (Entry<String, Object> entry : map.entrySet()) {
      if (Objects.nonNull(lastEntry)) {
        writeMapEntry(lastEntry);
        jsonOutput.write(",");
      }
      
      lastEntry = entry;
    };
    
    writeMapEntry(lastEntry);

    jsonOutput.write("}");
  }
  
  public void writeArray(String[] array)
  throws IOException {
    requireNonNull(array);
    
    if (array.length == 0)
      jsonOutput.write("[]");
    
    jsonOutput.write("[");
    
    String lastEntry = null;
    
    for (String entry : array) {
      if (Objects.nonNull(lastEntry)) {
        writeString(entry);
        jsonOutput.write(",");
      }
      
      lastEntry = entry;
    }
    
    writeString(lastEntry);
    jsonOutput.write("]");
  }
  
  public void close()
  throws IOException {
    jsonOutput.close();
  }
  
}
