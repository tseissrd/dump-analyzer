/*
 */
package com.example.springapp.dumpanalyzer.data;

import com.example.springapp.dumpanalyzer.config.AppConfiguration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import static java.util.Objects.nonNull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sovereign
 */
public class FileManager {
  
  private static final int IO_BUFFER_SIZE = 16384;
  
  private final String pathBase;
  private final FileSystem fs;
  
  public FileManager() {
    fs = FileSystems.getDefault();
    
    this.pathBase = fs.getPath(
      //System.getProperty("java.io.tmpdir"),
      System.getProperty("user.home"),
      "app-storage",
      "dump-analyzer"
    ).toAbsolutePath()
      .toString();
  }
  
  public FileManager(String pathBase) {
    this.pathBase = pathBase;
    fs = FileSystems.getDefault();
  }
  
  public FileManager(AppConfiguration config) {
    pathBase = config.get("custom.dumpanalyzer.data-path-base");
    fs = FileSystems.getDefault();
  }
  
  public void mkdir(String type)
  throws IOException {
    Path path = pathFor(type);
    
    if (Files.exists(path))
      throw new IOException("Path exists: " + path);
      
    Files.createDirectories(path);
  }
  
  public boolean dirExists(String type) {
    return Files.exists(pathFor(type));
  }
  
  public boolean fileExists(String file, String type) {
    return Files.exists(
      pathFor(file, type)
    );
  }
  
  private Path pathFor(String file, String type) {
    return fs.getPath(
      pathBase,
      type,
      file
    );
  }
  
  private Path pathFor(String type) {
    return fs.getPath(
      pathBase,
      type
    );
  }
  
  public InputStream getFile(String file, String type)
  throws IOException {
    return Files.newInputStream(
      pathFor(file, type)
    );
  }
  
  public OutputStream getSink(String file, String type)
  throws IOException {
    return Files.newOutputStream(
      pathFor(file, type)
    );
  }
  
  public String[] list(String type, Comparator<? super Path> comparator)
  throws IOException {
    Path path = pathFor(type);
    
    if (!Files.exists(path))
      return new String[0];

    String[] listing = Files.list(path)
      .filter(Files::isRegularFile)
      .map(Path::getFileName)
      .sorted(
        (path1, path2) -> comparator.compare(
          path.resolve(path1),
          path.resolve(path2)
        )
      )
      .map(Path::toString)
      .toArray(String[]::new);
    
    return listing;
  }
  
  public String view(String file, String type) throws IOException {
    Path path = pathFor(file, type);
        
    if (!Files.exists(path))
      throw new IOException("No such file: " + path);
    
    StringBuilder out = new StringBuilder();
    
    char[] buf = new char[IO_BUFFER_SIZE / 16];
    int read;
    
    try(BufferedReader contents = Files.newBufferedReader(
      path,
      Charset.forName("utf-8")
    )) {
      while ((read = contents.read(buf)) > -1)
        out.append(buf, 0, read);
    }
    
    return out.toString();
  }
  
  public void accept(String file, String type, InputStream data)
  throws IOException {
    Path path = pathFor(file, type);
    if (!dirExists(type))
      mkdir(type);
    
    byte[] buf = new byte[IO_BUFFER_SIZE];
    int read;
    
    try (OutputStream sink = Files.newOutputStream(path)) {
      while ((read = data.read(buf)) > -1)
        sink.write(buf, 0, read);
    } finally {
      data.close();
    }
  }
  
  public void remove(String file, String type) {
    Path path = pathFor(file, type);
    
    if (
      Files.exists(path)
      && Files.isRegularFile(path)
    ) {
      try {
        Files.delete(path);
      } catch (IOException ex) {
        Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  
  private void rmdir(Path path) {
    if (
      Files.exists(path)
      && Files.isDirectory(path)
    ) {
      try {
        Files.list(path)
          .forEach(file -> {
            if (Files.isRegularFile(file))
              try {
                Files.delete(file);
            } catch (IOException ex) {
              Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            else if (Files.isDirectory(file)) {
              rmdir(file);
            }
          });
        
        Files.delete(path);
      } catch (IOException ex) {
        Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }
  
  public void removeAll(String type) {
    Path path = pathFor(type);
    
    rmdir(path);
  }
  
  public long countLines(String file, String type)
  throws IOException {
    Path path = pathFor(file, type);
        
    if (!Files.exists(path))
      throw new IOException("No such file: " + path);
    
    long linesCount = 0;
        
    try(BufferedReader contents = Files.newBufferedReader(
      path,
      Charset.forName("utf-8")
    )) {
      while (
        nonNull(
          contents.readLine()
        )
      )
        linesCount += 1;
    }
        
    return linesCount;
  }
  
}
