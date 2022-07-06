/*
 */
package com.example.springapp.dumpanalyzer.data;

import com.example.springapp.dumpanalyzer.data.processor.IhsHttpAccessProcessor;
import com.example.springapp.dumpanalyzer.data.processor.Processor;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 *
 * @author Sovereign
 */
@Component
public class ProcessorManager {
  
  private final ExecutorService executor;
  
  private static final List<Processor> processors;
  
  static {
    processors = new LinkedList<>();
    processors.add(IhsHttpAccessProcessor.getInstance());
  }
  
  public ProcessorManager() {
    this(10);
  }
  
  public ProcessorManager(int threads) {
    executor = Executors.newFixedThreadPool(threads);
  }
  
  public Future<Void> process(final File in, final File out, final String type) {
    return (Future<Void>) executor.submit(() -> {
      for (Processor processor : processors) {
        if (processor.isAccepting(type)) {
          processor.process(in, out);
          return;
        }
      }
    });
  }
  
  
  
}
