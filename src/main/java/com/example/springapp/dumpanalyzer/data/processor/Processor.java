/*
 */
package com.example.springapp.dumpanalyzer.data.processor;

import java.io.File;

/**
 *
 * @author Sovereign
 */
public interface Processor {
  
  public boolean isAccepting(String type);
  
  public void process(File in, File out);
  
}
