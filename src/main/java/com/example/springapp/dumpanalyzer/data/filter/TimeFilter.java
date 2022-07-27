/*
 */
package com.example.springapp.dumpanalyzer.data.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import static java.util.Objects.isNull;

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
    return new StringBuilder(ID)
      .append(".")
      .append(from)
      .append("-")
      .append(to)
      .toString();
  }

  private boolean checkForEnd(String line) {
    if (to < 0)
      return false;

    if (
      linesRead > to
    )
      return true;
    else
      return false;
  }
  
  private long skipToStart(BufferedReader in)
  throws IOException {
    if (from < 0)
      return 0;

    for (long skipped = 0; skipped < from; skipped += 1) {
      System.out.println("skipped " + skipped);
      if (
        isNull(
          in.readLine()
        )
      ) {
        return skipped;
      }
    }
    
    return from;
  }
  
  @Override
  public BufferedReader filteredReader(BufferedReader in) {
    return new BufferedReader(in) {
      private boolean skippedToStart = false;
      private long linesRead = 0;

      @Override
      public String readLine()
      throws IOException {
        if (!skippedToStart) {
          linesRead = skipToStart(in);
          skippedToStart = true;
        }
        
        System.out.println(descriptor());

        String line = super.readLine();

        if (checkForEnd(linesRead))
          return null;
        
        System.out.println("checked");

        linesRead += 1;
        return line;
      }
    };
  }
}
