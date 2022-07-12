/*
 */
package com.example.springapp.dumpanalyzer.data;

import com.example.springapp.dumpanalyzer.data.processor.IhsHttpAccessProcessor;
import com.example.springapp.dumpanalyzer.data.processor.Processor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Sovereign
 */
public class ProcessorManager {
  
  private final ExecutorService executor;
  
  private static final List<Processor> processors;
  
  static {
    processors = new LinkedList<>();
    processors.add(
      IhsHttpAccessProcessor.getInstance()
    );
  }
  
  public ProcessorManager() {
    this(10);
  }
  
  public ProcessorManager(int threads) {
    executor = Executors.newFixedThreadPool(threads);
  }
  
  public Future<Void> process(
    final InputStream in,
    final OutputStream out,
    String type,
    String mode
  ) {
    try {
      return executor.submit(() -> {
        try {
          for (Processor processor : processors) {
            if (processor.accepts(type, mode)) {
              processor.process(
                in,
                out,
                type,
                mode
              );
              return (Void)null;
            }
          }
          return (Void)null;
        } finally {
          try {
            out.close();
          } finally {
            in.close();
          }
        }
      });
    } catch (Throwable ex) {
      try {
        try {
          in.close();
          out.close();
        } finally {
          out.close();
        }
      } catch (IOException ioEx) {
      }
      
      throw ex;
    }
  }
  
}
