/*
 */
package com.example.springapp.dumpanalyzer.heap.phd;

/**
 *
 * @author Sovereign
 */
public class IncorrectFormatException extends Exception {

  public IncorrectFormatException() {
    super();
  }
  
  public IncorrectFormatException(String message) {
    super(message);
  }
  
  public IncorrectFormatException(Throwable ex) {
    super(ex);
  }
  
}
