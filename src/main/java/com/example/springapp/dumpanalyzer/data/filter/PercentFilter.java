/*
 */
package com.example.springapp.dumpanalyzer.data.filter;

import java.io.BufferedReader;

/**
 *
 * @author Sovereign
 */
public class PercentFilter
implements Filter {
  
  public static final String ID = "PERCENT";
  
  private final long from;
  private final long to;
  private final LinesFilter innerFilter;
    
  public PercentFilter(long start, long end, long linesTotal) {
    this.from = start;
    this.to = end;
    innerFilter = new LinesFilter(
      Math.round(linesTotal / ((double)100) * start),
      Math.round(linesTotal / ((double)100) * end)
    );
  }
  
  public PercentFilter(String start, String end, long linesTotal) {
    long from = -1;
    long to = -1;
    
    try {
      from = Long.parseLong(start);
    } catch (Throwable ex) {}
    
    try {
      to = Long.parseLong(end);
    } catch (Throwable ex) {
      to = -1;
    }
    
    this.from = from;
    this.to = to;
    innerFilter = new LinesFilter(
      Math.round(linesTotal / ((double)100) * from),
      Math.round(linesTotal / ((double)100) * to)
    );
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
  
  @Override
  public BufferedReader filteredReader(BufferedReader in) {
    return innerFilter.filteredReader(in);
  }
}
