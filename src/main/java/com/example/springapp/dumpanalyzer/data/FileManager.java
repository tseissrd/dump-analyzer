/*
 */
package com.example.springapp.dumpanalyzer.data;

import com.example.springapp.dumpanalyzer.config.AppConfiguration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
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
  
  public String[] list(String type)
  throws IOException {
    Path path = pathFor(type);
    
    if (!Files.exists(path))
      return new String[0];

    System.out.println(path.normalize());

    String[] listing = Files.list(path)
      .filter(Files::isRegularFile)
      .map(Path::getFileName)
      .map(Path::toString)
      .toArray(String[]::new);
    
    return listing;
  }
  
  public String view(String file, String type) throws IOException {
    Path path = pathFor(file, type);
    
    System.out.println(path.normalize());
    
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
  
}
