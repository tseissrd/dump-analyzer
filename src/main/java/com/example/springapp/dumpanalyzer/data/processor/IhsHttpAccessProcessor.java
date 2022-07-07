/*
 */
package com.example.springapp.dumpanalyzer.data.processor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Objects;
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
  public void process(InputStream in, OutputStream out) {
    try (BufferedReader reader = new BufferedReader(
      new InputStreamReader(in, Charset.forName("utf-8"))
    )) {
      try (BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(
          out,
          Charset.forName("utf-8")
        )
      )) {
        writer.append("{\"out\":\"");
        
        String line;
        
        while (
          Objects.nonNull(
            line = reader.readLine()
          )
        ) {
          System.out.println(line);
          line = line
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
          writer.append(line);
        }
        
        writer.append("\"}");
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
  public boolean accepts(String type) {
    return true;
  }
  
}
