/*
 */
package com.example.springapp.dumpanalyzer.data.json;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import static java.util.Objects.requireNonNull;

/**
 *
 * @author Sovereign
 */
public class JsonOutputWriter
implements Closeable, Flushable {
  
  private final BufferedWriter jsonOutput;
  
  private int depth;
  private boolean nextWithComma;
  
  public JsonOutputWriter(Writer sink) {
    this.jsonOutput = new BufferedWriter(
      sink
    );
    
    depth = 0;
    nextWithComma = false;
  }
  
  @SuppressWarnings("unchecked")
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
    } else if (object instanceof List) {
      writeList((List)object);
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
  
  private void startArray()
  throws IOException {
    jsonOutput.write("[");
    
    this.depth += 1;
    this.nextWithComma = false;
  }
  
  private void endArray()
  throws IOException {
    jsonOutput.write("]");
    
    this.depth -= 1;
    
    if (this.depth <= 0)
      this.nextWithComma = false;
    else
      this.nextWithComma = true;
  }
  
  // TODO
  private void optionalComma()
  throws IOException {
    if (this.nextWithComma)
      jsonOutput.write(",");
  }
  
  public void writeRaw(String data)
  throws IOException {
    jsonOutput.write(data);
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
  
  private void writeMapEntry(Entry entry)
  throws IOException {
    jsonOutput.write(
      new StringBuilder("\"")
        .append(
          entry.getKey()
            .toString()
        )
        .append("\":")
        .toString()
    );
    write(entry.getValue());
  }
  
  public <T> void writeMap(Map map)
  throws IOException {
    if (Objects.isNull(map))
      return;
      // return "";
    
    jsonOutput.write("{");
    
    Entry lastEntry = null;
    
    for (Object entry : map.entrySet()) {
      if (Objects.nonNull(lastEntry)) {
        writeMapEntry(lastEntry);
        jsonOutput.write(",");
      }
      
      lastEntry = (Entry)entry;
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
    
    if (
      Objects.nonNull(lastEntry)
    ) {
      writeString(lastEntry);
    }
    
    jsonOutput.write("]");
  }
  
  public void writeList(List list)
  throws IOException {
    requireNonNull(list);
    
    if (list.isEmpty())
      jsonOutput.write("[]");
    
    jsonOutput.write("[");
    
    Object lastEntry = null;
    
    for (Object entry : list) {
      if (Objects.nonNull(lastEntry)) {
        write(lastEntry);
        jsonOutput.write(",");
      }
      
      lastEntry = entry;
    }
    
    if (
      Objects.nonNull(lastEntry)
    ) {
      write(lastEntry);
    }
    
    jsonOutput.write("]");
  }
  
  public void close()
  throws IOException {
    jsonOutput.close();
  }

  @Override
  public void flush()
  throws IOException {
    jsonOutput.flush();
  }
  
}
