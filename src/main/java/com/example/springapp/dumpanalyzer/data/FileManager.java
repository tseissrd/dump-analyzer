/*
 */
package com.example.springapp.dumpanalyzer.data;

import com.example.springapp.dumpanalyzer.config.AppConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 *
 * @author Sovereign
 */
@Configuration
public class FileManager {
  
  private static final int IO_BUFFER_SIZE = 16384;
  
  private final String pathBase;
  private final Set<File> filesToProcess;
  
  @Autowired
  private ProcessorManager processorManager;
  
  @Autowired
  public FileManager(AppConfiguration config) {
    pathBase = config.get("custom.dumpanalyzer.data-path-base");
    filesToProcess = new HashSet<>();
  }
  
  public void mkdir(String type) throws IOException {
    File path = new File(pathBase + "/" + type + "/out");
    
    if (path.exists())
      throw new IOException("Path exists: " + path);
      
    path.mkdirs();
  }
  
  public boolean dirExists(String type) {
    File path = new File(pathBase + "/" + type);
    
    return path.exists();
  }
  
  public String[] list(String type) {
    File path = new File(pathBase + "/" + type);
    
    if (!path.exists())
      return new String[0];

    System.out.println(path.getAbsolutePath());

    File[] listing = path.listFiles();
    String[] out = new String[listing.length];

    int numberOfFiles = 0;
    
    for (int fileNum = 0; fileNum < out.length; fileNum += 1) {
      if (listing[fileNum].isFile()) {
        out[numberOfFiles] = listing[fileNum].getName();
        numberOfFiles += 1;
      }
    }
    
    out = Arrays.copyOf(out, numberOfFiles);
    
    return out;
  }
  
  public String view(String file, String type) throws IOException {
    File path = new File(pathBase + "/" + type + "/" + file);
    
    System.out.println(path.getAbsolutePath());
    
    if (!path.exists())
      throw new IOException("No such file: " + path.getAbsolutePath());
    
    File processedFile = new File(
      path.getParent()
      + "/out/"
      + path.getName()
      + ".json"
    );
    
    if (!processedFile.exists())
      try {
        processorManager.process(path, processedFile, type)
          .get();
    } catch (Throwable ex) {
      throw new RuntimeException(ex);
    }
    
    StringBuilder out = new StringBuilder();
    
    char[] buf = new char[IO_BUFFER_SIZE / 16];
    int read;
    
    try(FileReader contents = new FileReader(processedFile)) {
      while ((read = contents.read(buf)) > -1)
        out.append(buf, 0, read);
    }
    
    return out.toString();
  }
  
  public void accept(String file, String type, InputStream data)
  throws IOException {
    File path = new File(pathBase + "/" + type + "/" + file);
    if (!dirExists(type))
      mkdir(type);
    
    byte[] buf = new byte[IO_BUFFER_SIZE];
    int read;
    
    try (FileOutputStream sink = new FileOutputStream(path)) {
      while ((read = data.read(buf)) > -1)
        sink.write(buf, 0, read);
    }
    
    File processedFilePath = new File(pathBase + "/" + type + "/out/" + file + ".json");
    
    processorManager.process(path, processedFilePath, type);
  }
  
}
