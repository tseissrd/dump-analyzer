/*
 */
package com.example.springapp.dumpanalyzer.data;

import com.example.springapp.dumpanalyzer.data.processor.IhsHttpAccessProcessor;
import com.example.springapp.dumpanalyzer.data.processor.Processor;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
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
  
  private static final FileSystem fs = FileSystems.getDefault();
  
  private final FileManager fileManager;
  
  static {
    processors = new LinkedList<>();
    processors.add(IhsHttpAccessProcessor.getInstance());
  }
  
  public ProcessorManager(FileManager fileManager) {
    this(fileManager, 10);
  }
  
  public ProcessorManager(FileManager fileManager, int threads) {
    executor = Executors.newFixedThreadPool(threads);
    this.fileManager = fileManager;
  }
  
  public Future<Void> process(final InputStream in, final OutputStream out, String type) {
    return (Future<Void>) executor.submit(() -> {
      for (Processor processor : processors) {
        if (processor.accepts(type)) {
          processor.process(in, out);
          return;
        }
      }
    });
  }
  
}
