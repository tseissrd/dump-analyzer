/*
 */
package com.example.springapp.dumpanalyzer.data.filter;

import java.io.BufferedReader;
import java.io.IOException;
import static java.util.Objects.isNull;

/**
 *
 * @author Sovereign
 */
public class LinesFilter
implements Filter {
  
  public static final String ID = "LINES";
  
  private final long from;
  private final long to;
    
  public LinesFilter(long start, long end) {
    this.from = start;
    this.to = end;
  }
  
  public LinesFilter(String start, String end) {
    long from = -1;
    long to = -1;
    
    try {
      from = Long.parseLong(start);
    } catch (Throwable ex) {}
    
    this.from = from;
    
    try {
      to = Long.parseLong(end);
    } catch (Throwable ex) {
      to = -1;
    }
    
    this.to = to;
  }

  public long getStart() {
    return from;
  }

  public long getEnd() {
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

  private boolean checkForEnd(long linesRead) {
    if (to < 0)
      return false;

    if (
      linesRead >= to
    )
      return true;
    else
      return false;
  }
  
  private long skipToStart(BufferedReader in)
  throws IOException {
    if (from < 0)
      return 0;
    
    long skipToLine = Math.max(from - 1, 0);

    for (long skipped = 0; skipped < skipToLine; skipped += 1) {
      if (
        isNull(
          in.readLine()
        )
      ) {
        return skipped;
      }
    }
    
    return skipToLine;
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
        
        String line = super.readLine();

        if (checkForEnd(linesRead))
          return null;
        
        linesRead += 1;
        return line;
      }
    };
  }
}
