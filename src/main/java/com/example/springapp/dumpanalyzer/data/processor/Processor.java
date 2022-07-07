/*
 */
package com.example.springapp.dumpanalyzer.data.processor;

import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Sovereign
 */
public interface Processor {
  
  public boolean accepts(String type);
  
  public void process(InputStream in, OutputStream out);
  
}
