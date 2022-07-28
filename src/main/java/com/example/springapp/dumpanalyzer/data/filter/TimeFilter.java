/*
 */
package com.example.springapp.dumpanalyzer.data.filter;

import com.example.springapp.dumpanalyzer.data.processor.HttpAccessRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 *
 * @author Sovereign
 */
public class TimeFilter
implements Filter {
  
  public static final String ID = "TIME";
  
  private final Instant from;
  private final Instant to;
    
  public TimeFilter(Instant start, Instant end) {
    this.from = start;
    this.to = end;
  }
  
  public TimeFilter(String start, String end) {
    Instant from = null;
    Instant to = null;
    
    try {
      from = Instant.parse(start);
    } catch (Throwable ex) {}
    
    this.from = from;
    
    try {
      to = Instant.parse(end);
    } catch (Throwable ex) {
      to = null;
    }
    
    this.to = to;
  }

  public Instant getStart() {
    return from;
  }

  public Instant getEnd() {
    return to;
  }
  
  @Override
  public String descriptor() {
    StringBuilder descriptor = new StringBuilder(ID)
      .append(".");
    
    if (
      nonNull(from)
    ) {
      descriptor.append(
        from.toString()
          .replace(':', '_')
      );
    } else {
      descriptor.append("ANY");
    }
    
    descriptor.append("-");
    
    if (
      nonNull(to)
    ) {
      descriptor.append(
        to.toString()
          .replace(':', '_')
      );
    } else {
      descriptor.append("ANY");
    }
    
    return descriptor.toString();
  }

  private boolean checkForEnd(String line) {
    if (
      isNull(to)
    )
      return false;

    HttpAccessRecord record;
    
    try {
      record = new HttpAccessRecord(line);
    } catch (Throwable ex) {
      return false;
    }
    
    if (
      record.getDate()
        .compareTo(to) > 0
    )
      return true;
    else
      return false;
  }
  
  private String skipToStart(BufferedReader in)
  throws IOException {
    if (
      isNull(
        from
      )
    )
      return in.readLine();
    
    HttpAccessRecord record;
    String line = null;

    while(
      nonNull(
        line = in.readLine()
      )
    ) {
      record = new HttpAccessRecord(line);
      
      if (
        record.getDate()
          .compareTo(from) >= 0
      ) {
        break;
      }
    }  
    
    return line;
  }
  
  @Override
  public BufferedReader filteredReader(BufferedReader in) {
    return new BufferedReader(in) {
      private boolean skippedToStart = false;

      @Override
      public String readLine()
      throws IOException {
        String line = null;
        
        if (!skippedToStart) {
          line = skipToStart(in);
          skippedToStart = true;
          
          if (
            isNull(
              line
            )
          )
            return null;
        }

        if (
          isNull(line)
        )
          line = super.readLine();

        if (checkForEnd(line))
          return null;

        return line;
      }
    };
  }
}
