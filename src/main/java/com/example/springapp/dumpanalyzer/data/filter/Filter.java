/*
 */
package com.example.springapp.dumpanalyzer.data.filter;

/**
 *
 * @author Sovereign
 */
public final class Filter {
  
  public static final Filter DEFAULT = getInstance(
    FilterMode.NONE,
    null,
    null
  );
  
  public static final Filter NONE = DEFAULT;
  
  public enum FilterMode {
    NONE,
    TIME,
    LINES,
    PERCENT
  }
  
  private final FilterMode mode;
  private final String from;
  private final String to;
    
  private Filter(FilterMode mode, String start, String end) {
    this.mode = mode;
    this.from = start;
    this.to = end;
  }

  public String getStart() {
    return from;
  }

  public String getEnd() {
    return to;
  }

  public FilterMode getMode() {
    return mode;
  }
  
  public String descriptor() {
    return new StringBuilder(mode.toString())
      .append(from)
      .append("-")
      .append("to")
      .toString();
  }
  
  public static final Filter getInstance(FilterMode mode, String start, String end) {
    return new Filter(mode, start, end);
  }
  
}
