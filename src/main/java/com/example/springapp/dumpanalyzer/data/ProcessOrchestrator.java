/*
 */
package com.example.springapp.dumpanalyzer.data;

import com.example.springapp.dumpanalyzer.config.AppConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Sovereign
 */
@Component
public class ProcessOrchestrator {
  
  private final FileManager fileManager;
  private final ProcessorManager processorManager;
  private final Map<String, Map<String, ManagedFile>> managedFiles;
  private final Map<ManagedFile, Future<Void>> filesInProcessing;
  private final Thread cleanupThread;
  
  private static final Pattern JSON_EXTENSION_PATTERN = Pattern.compile(".json$");
  
  @Autowired
  protected ProcessOrchestrator(AppConfiguration config) {
    fileManager = new FileManager(
      config.get("custom.dumpanalyzer.data-path-base")
    );
    
    String processorThreadsConfig = config.get("custom.dumpanalyzer.processor-threads");
    
    if (Objects.nonNull(processorThreadsConfig)) {
      processorManager = new ProcessorManager(
        fileManager,
        Integer.parseInt(processorThreadsConfig)
      );
    } else {
      processorManager = new ProcessorManager(
        fileManager
      );
    }
    
    filesInProcessing = new ConcurrentHashMap<>();
    managedFiles = new ConcurrentHashMap<>();
    
    cleanupThread = new Thread() {
      @Override
      public void run() {
        Future<Void> processing;
        
        while (!Thread.interrupted()) {
          try {
            Thread.sleep(3000);
            
            for (ManagedFile managedFile : filesInProcessing.keySet()) {
              processing = filesInProcessing.get(managedFile);
              
              if (processing.isDone()) {
                synchronized(managedFiles) {
                  filesInProcessing.remove(managedFile);

                  stopManagingFile(
                    managedFile.getName(),
                    managedFile.getType()
                  );
                  processing = null;
                }
              }
            }
          } catch (InterruptedException ex) {
            return;
          }
        }
      }
    };
    
    cleanupThread.start();
  }
  
  private boolean isFileManaged(String file, String type) {
    synchronized(managedFiles) {
      if (!managedFiles.containsKey(type))
        return false;

      Map<String, ManagedFile> filesOfType = managedFiles.get(type);

      return filesOfType.containsKey(file);
    }
  }
  
  private ManagedFile getManagedFile(String file, String type)
  throws NoSuchElementException {
    synchronized(managedFiles) {
      if (!isFileManaged(file, type))
        throw new NoSuchElementException(String.format("managed: (type: %s, file: %s)", type, file));

      Map<String, ManagedFile> filesOfType = managedFiles.get(type);

      return filesOfType.get(file);
    }
  }
  
  private ManagedFile manageFile(String file, String type) {
    synchronized(managedFiles) {
      if (isFileManaged(file, type))
        return getManagedFile(file, type);

      if (!(managedFiles.containsKey(type)))
        managedFiles.put(type, new ConcurrentHashMap<>());

      Map<String, ManagedFile> filesOfType = managedFiles.get(type);

      ManagedFile managed = new ManagedFile(file, type);

      filesOfType.put(file, managed);

      return managed;
    }
  }
  
  private void stopManagingFile(String file, String type) {
    if (!isFileManaged(file, type))
      throw new NoSuchElementException(String.format("managed: (type: %s, file: %s)", type, file));
    
    managedFiles.get(type)
      .remove(file);
    
    if (
      managedFiles.get(type)
        .isEmpty()
    )
      managedFiles.remove(type);
  }
  
  public void mkdir(String type)
  throws IOException {
    fileManager.mkdir(type + "/out");
  }
  
  public boolean dirExists(String type) {
    return fileManager.dirExists(type);
  }
  
  public String[] list(String type)
  throws IOException {
    System.out.println("!!!" + fileManager.list(type).length);
    
    return Arrays.stream(
        fileManager.list(type + "/out")
      ).map(path -> JSON_EXTENSION_PATTERN.matcher(path)
        .replaceFirst(""))
        .toArray(String[]::new);
  }
  
  public String view(String file, String type)
  throws IOException {
    String processedFileName = file + ".json";
    String processedType = type + "/out";
    
    if (!fileManager.dirExists(type))
      throw new NoSuchFileException(
        String.format(
          "type: %s, file: %s",
          processedFileName,
          processedType
        )
      );
    
    if (fileManager.fileExists(processedFileName, processedType))
      return fileManager.view(processedFileName, processedType);
    else {
      if (!fileManager.dirExists(processedType))
        fileManager.mkdir(processedType);
      
      Future<Void> processing = processFile(file, type);
      
      try {
        processing.get();
      } catch (InterruptedException ex) {
        Logger.getLogger(ProcessOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
      } catch (ExecutionException ex) {
        Logger.getLogger(ProcessOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
      }
      return fileManager.view(processedFileName, processedType);
    }
  }
  
  private Future<Void> processFile(String file, String type)
  throws IOException {
    Future<Void> processing;

    ManagedFile managedFile;
    
    synchronized(managedFiles) {
      if (
        isFileManaged(file, type)
      ) {
        managedFile = getManagedFile(file, type);

        if (filesInProcessing.containsKey(managedFile))
          return  filesInProcessing.get(managedFile);
      }
    
      managedFile = manageFile(file, type);

      processing = processorManager.process(
        fileManager.getFile(
          file,
          type
        ),
        fileManager.getSink(
          file + ".json",
          type + "/out"
        ),
        type
      );

      filesInProcessing.put(
        managedFile,
        processing
      );

      return processing;
    }
  }
  
  private Future<Void> processData(String file, String type, InputStream data)
  throws IOException {
    Future<Void> processing;

    ManagedFile managedFile;
    
    synchronized(managedFiles) {
      if (
        isFileManaged(file, type)
      ) {
        managedFile = getManagedFile(file, type);

        if (filesInProcessing.containsKey(managedFile))
          return  filesInProcessing.get(managedFile);
      }
    
      managedFile = manageFile(file, type);

      processing = processorManager.process(
        data,
        fileManager.getSink(
          file + ".json",
          type + "/out"
        ),
        type
      );

      filesInProcessing.put(
        managedFile,
        processing
      );

      return processing;
    }
  }
  
  public void accept(String file, String type, InputStream data)
  throws IOException {
    if (fileManager.fileExists(file, type)) {
      if (!isFileManaged(file, type)) {
      }
      
      throw new IOException(String.format("file (type: %s, file: %s) exists.", type, file));
    }
    
    Future<Void> process = processData(file, type, data);
  }
  
  public void remove(String file, String type) {
    fileManager.remove(file + ".json", type + "/out");
    fileManager.remove(file, type);
  }
  
}
