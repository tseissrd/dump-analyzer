/*
 */
package com.example.springapp.dumpanalyzer.data.processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sovereign
 */
public class IhsHttpAccessProcessor
implements Processor {
  
  private static final IhsHttpAccessProcessor singleton;
  
  static {
    singleton = new IhsHttpAccessProcessor();
  }
  
  protected IhsHttpAccessProcessor() {}
  
  public static IhsHttpAccessProcessor getInstance() {
    return singleton;
  }

  @Override
  public void process(File in, File out) {
    try (BufferedReader reader = new BufferedReader(
      new FileReader(in)
    )) {
      try (BufferedWriter writer = new BufferedWriter(
        new FileWriter(out)
      )) {
        String line = reader.readLine();
        System.out.println(line);
        writer.append(line);
      }
    } catch (FileNotFoundException ex) {
      Logger.getLogger(IhsHttpAccessProcessor.class.getName()).log(Level.SEVERE, null, ex);
      return;
    } catch (IOException ex) {
      Logger.getLogger(IhsHttpAccessProcessor.class.getName()).log(Level.SEVERE, null, ex);
      return;
    }
  }

  @Override
  public boolean isAccepting(String type) {
    return true;
  }
  
}
