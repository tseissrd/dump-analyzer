/*
 */
package com.example.springapp.dumpanalyzer.data.processor;

import com.example.springapp.dumpanalyzer.data.filter.Filter;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Sovereign
 */
public interface Processor {
  
  public boolean accepts(
    String type,
    String mode
  );
  
  public void process(
    InputStream in,
    OutputStream out,
    String type,
    String mode,
    Filter filter
  );
  
}
