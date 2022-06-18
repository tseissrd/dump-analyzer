/*
 */
package com.example.springapp.dumpanalyzer.data;

import com.example.springapp.dumpanalyzer.config.AppConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
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
  
  private final Map<String, List<String>> data;
  private String pathBase;
  
  @Autowired
  public FileManager(AppConfiguration config) {
    data = new WeakHashMap<>();
    pathBase = config.get("custom.dumpanalyzer.data-path-base");
  }
  
  public void mkdir(String folder) throws IOException {
    File path = new File(pathBase + "/" + folder);
    
    if (path.exists())
      throw new IOException("Path exists: " + path);
      
    path.mkdirs();
  }
  
  public String[] list(String folder) {
    File path = new File(pathBase + "/" + folder);
    
    if (!path.exists())
      return new String[0];
    
    if (!data.containsKey(folder)) {
      data.put(folder, new ArrayList<>());
    }

    System.out.println(path.getAbsolutePath());

    File[] listing = path.listFiles();
    String[] out = new String[listing.length];

    for (int fileNum = 0; fileNum < out.length; fileNum += 1)
      out[fileNum] = listing[fileNum].getName();
    
    return out;
  }
  
  public String view(String file) throws IOException {
    File path = new File(pathBase + "/" + file);
    
    System.out.println(path.getAbsolutePath());
    
    if (!path.exists())
      throw new IOException("No such file: " + path.getAbsolutePath());
    
    StringBuilder out = new StringBuilder();
    
    char[] buf = new char[1024];
    int read;
    FileReader contents = new FileReader(path);
    
    while ((read = contents.read(buf)) > -1)
      out.append(buf, 0, read);
    
    return out.toString();
  }
  
}
