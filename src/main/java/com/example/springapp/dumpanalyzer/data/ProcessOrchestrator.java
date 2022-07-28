/*
 */
package com.example.springapp.dumpanalyzer.data;

import com.example.springapp.dumpanalyzer.config.AppConfiguration;
import com.example.springapp.dumpanalyzer.data.filter.Filter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
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
  
  private final String TEXT_MODE = "text";
  
  private final FileManager fileManager;
  private final ProcessorManager processorManager;
  private final Map<String, Map<String, ManagedFile>> managedFiles;
  private final Set<ManagedFile> filesInProcessing;
  private Thread cleanupThread;
  
  @Autowired
  protected ProcessOrchestrator(AppConfiguration config) {
    fileManager = new FileManager(
      // config.get("custom.dumpanalyzer.data-path-base")
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
    
    return fileManager.list(
      type,
      (path1, path2) -> {
        try {
          return Files.getLastModifiedTime(path2)
            .compareTo(
              Files.getLastModifiedTime(path1)
            );
        } catch (IOException ex) {
          Logger.getLogger(ProcessOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return path1.compareTo(path2);
      }
    );
  }
  
  public String getProcessedType(
    String file,
    String type,
    String mode,
    Filter filter
  ) {
    if (
      mode.equals("text")
      && filter.equals(Filter.NOOP)
    )
      return type;
    
    return new StringBuilder(type)
      .append("/")
      .append(file)
      .append(".cached")
      .toString();
  }
  
  public String getProcessedFileName(
    String file,
    String type,
    String mode,
    Filter filter
  ) {
    if (
      mode.equals("text")
      && filter.equals(Filter.NOOP)
    )
      return file;
    
    return new StringBuilder(file)
      .append(".")
      .append(mode)
      .append(".")
      .append(
        filter.descriptor()
      )
      .append(".json")
      .toString();
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
  
  public String view(String file, String type, String mode, Filter filter)
  throws IOException {
    String processedFileName = getProcessedFileName(
      file,
      type,
      mode,
      filter
    );
    
    String processedType = getProcessedType(
      file,
      type,
      mode,
      filter
    );
    
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
      
      Future<Void> processing = processFile(
        file,
        type,
        mode,
        filter
      );
      
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
  
  public String view(String file, String type, String mode)
  throws IOException {
    return view(file, type, mode, Filter.NOOP);
  }
  
//  TODO
//  public InputStream getFile(String file, String type, String mode)
//  throws IOException {
//    String processedFileName = getProcessedFileName(file, type, mode);
//    String processedType = getProcessedType(file, type, mode);
//    
//    if (!fileManager.dirExists(type))
//      throw new NoSuchFileException(
//        String.format(
//          "type: %s, file: %s",
//          processedFileName,
//          processedType
//        )
//      );
//    
//    if (fileManager.fileExists(processedFileName, processedType))
//      return fileManager.view(processedFileName, processedType);
//    else {
//      if (!fileManager.dirExists(processedType))
//        fileManager.mkdir(processedType);
//      
//      Future<Void> processing = processFile(file, type, mode);
//      
//      try {
//        processing.get();
//      } catch (InterruptedException ex) {
//        Logger.getLogger(ProcessOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
//      } catch (ExecutionException ex) {
//        Logger.getLogger(ProcessOrchestrator.class.getName()).log(Level.SEVERE, null, ex);
//      }
//      return fileManager.view(processedFileName, processedType);
//    }
//  }
  
  public long countLines(String file, String type)
  throws IOException {
    String processedFileName = getProcessedFileName(
      file,
      type,
      TEXT_MODE
    );
    
    String processedFileType = getProcessedType(
      file,
      type,
      TEXT_MODE
    );
    
    return fileManager.countLines(
      processedFileName,
      processedFileType
    );
  }
  
  private Future<Void> processFile(
    String file,
    String type,
    String mode,
    Filter filter
  )
  throws IOException {
    String processedFileName = getProcessedFileName(
      file,
      type,
      mode,
      filter
    );
    String processedType = getProcessedType(
      file,
      type,
      mode,
      filter
    );
    
    Future<Void> processing;

    ManagedFile managedFile;
    
    String stateString = mode + "/" + filter.descriptor();
    
    synchronized(managedFiles) {
      if (
        isFileManaged(file, type)
      ) {
        managedFile = getManagedFile(file, type);

        if (
          filesInProcessing.contains(managedFile)
          // && managedFile.hasSavedState(mode)
          && managedFile.hasSavedState(stateString)
        )
          return  managedFile.getState(stateString);
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
        mode,
        filter
      );

      managedFile.saveState(stateString, processing);
      
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
    Filter filter,
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
        mode,
        filter
      );

      managedFile.saveState(mode, processing);
      filesInProcessing.add(managedFile);

      return processing;
    }
  }
  
  public void accept(String file, String type, InputStream data)
  throws IOException {
    String fileNameWithTimestamp = new StringBuilder(file)
      .append("@")
      .append(
        Instant.now()
          .truncatedTo(ChronoUnit.SECONDS)
          .toString()
          .replace(':', '_')
      ).toString();
    
    if (fileManager.fileExists(fileNameWithTimestamp, type)) {
      if (!isFileManaged(fileNameWithTimestamp, type)) {
      }
      
      throw new IOException(String.format("file (type: %s, file: %s) exists.", type, fileNameWithTimestamp));
    }
    
    fileManager.accept(fileNameWithTimestamp, type, data);
  }
  
  public void remove(String file, String type) {
    requireNonNull(file);
    requireNonNull(type);
    
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
