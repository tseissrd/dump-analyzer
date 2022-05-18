/*
 */
package com.example.springapp.dumpanalyzer;

import com.example.springapp.dumpanalyzer.heap.phd.IncorrectFormatException;
import com.example.springapp.dumpanalyzer.heap.phd.PhdDump;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sovereign
 */
public class Test {
  
  public static void main(String[] args) throws Throwable {
    File file = new File("./dump.phd");
    
    PhdDump dump;
    
    try {
      dump = new PhdDump(file);
    } catch (IOException ex) {
      Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IncorrectFormatException ex) {
      Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
    }
    
  }
  
}
