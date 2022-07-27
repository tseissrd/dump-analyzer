/*
 */
package com.example.springapp.dumpanalyzer.data.filter;

import java.io.BufferedReader;

/**
 *
 * @author Sovereign
 */
public interface Filter {
  
  public static final Filter NOOP = new Filter() {
    @Override
    public String descriptor() {
      return new StringBuilder("NOOP")
        .toString();
    }

    @Override
    public BufferedReader filteredReader(BufferedReader in) {
      return in;
    }
  };
  
  public String descriptor();
  public BufferedReader filteredReader(BufferedReader in);
  
}
