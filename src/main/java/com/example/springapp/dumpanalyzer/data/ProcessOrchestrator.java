/*
 */
package com.example.springapp.dumpanalyzer.data;

import com.example.springapp.dumpanalyzer.config.AppConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import static java.util.Objects.nonNull;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
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
  private final Set<ManagedFile> filesInProcessing;
  private Thread cleanupThread;
  
  @Autowired
  protected ProcessOrchestrator(AppConfiguration config) {
    fileManager = new FileManager(
      config.get("custom.dumpanalyzer.data-path-base")
    );
    
    String processorThreadsConfig = config.get("custom.dumpanalyzer.processor-threads");
    
    if (Objects.nonNull(processorThreadsConfig)) {
      processorManager = new ProcessorManager(
        Integer.parseInt(processorThreadsConfig)
      );
    } else {
      processorManager = new ProcessorManager();
    }
    
    filesInProcessing = new CopyOnWriteArraySet<>();
    managedFiles = new ConcurrentHashMap<>();
    
    runCleanups();
  }
  
  public final void runCleanups() {
    if (
      nonNull(cleanupThread)
      && cleanupThread.isAlive()
      && (!cleanupThread.isInterrupted())
    )
      return;
    
    cleanupThread = new Thread() {
      @Override
      public void run() {
        while (!Thread.interrupted()) {
          try {
            Thread.sleep(3000);
            
            runProcessingCleanup();
          } catch (InterruptedException ex) {
            return;
          }
        }
      }
    };
    
    cleanupThread.start();
  }
  
  public void stopCleanups() {
    if (
      nonNull(cleanupThread)
      && cleanupThread.isAlive()
      && (!cleanupThread.isInterrupted())
    )
      cleanupThread.interrupt();
  }
  
  private void runProcessingCleanup() {
    boolean removeFromProcessing;
            
    for (ManagedFile managedFile : filesInProcessing) {

      removeFromProcessing = !(
        managedFile.getAllStates()
          .entrySet()
          .stream()
          .parallel()
          .anyMatch(entry ->
            !(
              entry.getValue()
                .isDone()
            )
          )
      );

      if (removeFromProcessing) {
        synchronized(managedFiles) {
          filesInProcessing.remove(managedFile);

          stopManagingFile(
            managedFile.getName(),
            managedFile.getType()
          );
        }
      }
    }
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
    
    ManagedFile managedFile = getManagedFile(file, type);
    
    filesInProcessing.remove(managedFile);
    
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
    
    return fileManager.list(type);
  }
  
  public String getProcessedType(String file, String type, String mode) {
    if (mode.equals("text"))
      return type;
    
    return new StringBuilder(type)
      .append("/")
      .append(file)
      .append(".cached")
      .toString();
  }
  
  public String getProcessedFileName(String file, String type, String mode) {
    if (mode.equals("text"))
      return file;
    
    return new StringBuilder(file)
      .append(".")
      .append(mode)
      .append(".json")
      .toString();
  }
  
  public String view(String file, String type, String mode)
  throws IOException {
    String processedFileName = getProcessedFileName(file, type, mode);
    String processedType = getProcessedType(file, type, mode);
    
    System.out.println(processedFileName);
    System.out.println(processedType);
    System.out.println(mode);
    
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
      
      Future<Void> processing = processFile(file, type, mode);
      
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
  
  private Future<Void> processFile(String file, String type, String mode)
  throws IOException {
    String processedFileName = getProcessedFileName(file, type, mode);
    String processedType = getProcessedType(file, type, mode);
    
    Future<Void> processing;

    ManagedFile managedFile;
    
    synchronized(managedFiles) {
      if (
        isFileManaged(file, type)
      ) {
        managedFile = getManagedFile(file, type);

        if (
          filesInProcessing.contains(managedFile)
          && managedFile.hasSavedState(mode)
        )
          return  managedFile.getState(mode);
      }
    
      managedFile = manageFile(file, type);

      processing = processorManager.process(
        fileManager.getFile(
          file,
          type
        ),
        fileManager.getSink(
          processedFileName,
          processedType
        ),
        type,
        mode
      );

      managedFile.saveState(mode, processing);
      
      filesInProcessing.add(
        managedFile
      );

      return processing;
    }
  }
  
  private Future<Void> processData(
    String file,
    String type,
    String mode,
    InputStream data
  )
  throws IOException {
    String processedFileName = getProcessedFileName(file, type, mode);
    String processedType = getProcessedType(file, type, mode);
    
    Future<Void> processing;

    ManagedFile managedFile;
    
    synchronized(managedFiles) {
      if (
        isFileManaged(file, type)
      ) {
        managedFile = getManagedFile(file, type);

        if (
          filesInProcessing.contains(managedFile)
          && managedFile.hasSavedState(mode)
        )
          return managedFile.getState(mode);
      }
    
      managedFile = manageFile(file, type);

      processing = processorManager.process(
        data,
        fileManager.getSink(
          processedFileName,
          processedType
        ),
        type,
        mode
      );

      managedFile.saveState(mode, processing);
      filesInProcessing.add(managedFile);

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
    
    fileManager.accept(file, type, data);
  }
  
  public void remove(String file, String type) {
    if (isFileManaged(
      file,
      type
    ))
      stopManagingFile(file, type);
    
    fileManager.removeAll(
      new StringBuilder(type)
        .append("/")
        .append(file)
        .append(".cached")
        .toString()
    );
    fileManager.remove(file, type);
  }
  
}
