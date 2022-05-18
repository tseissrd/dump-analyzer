/*
 */
package com.example.springapp.dumpanalyzer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 *
 * @author Sovereign
 */
public class ReversableFileInputStream extends InputStream {

  private final RandomAccessFile file;
  private final byte[] buffer;
  private long position;
  private int positionInBuffer;
  
  public ReversableFileInputStream(File file, int bufferSize) throws FileNotFoundException {
    this.file = new RandomAccessFile(file, "r");
    this.buffer = new byte[bufferSize];
    position = 0;
    positionInBuffer = -1;
  }
  
  @Override
  public int read() throws IOException {
    
  }
  
  
  
}
