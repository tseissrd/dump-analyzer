/*
 */
package com.example.springapp.dumpanalyzer.heap.phd;

import com.example.springapp.dumpanalyzer.heap.phd.record.PhdClassRecord;
import com.example.springapp.dumpanalyzer.heap.phd.record.PhdLongObjectRecord;
import com.example.springapp.dumpanalyzer.heap.phd.record.PhdMediumObjectRecord;
import com.example.springapp.dumpanalyzer.heap.phd.record.PhdPrimitiveArrayRecord;
import com.example.springapp.dumpanalyzer.heap.phd.record.PhdRecord;
import com.example.springapp.dumpanalyzer.heap.phd.record.PhdShortObjectRecord;
import com.example.springapp.dumpanalyzer.heap.phd.record.PhdTag;
import com.example.springapp.dumpanalyzer.heap.phd.record.UnknownTagException;
import static com.example.springapp.dumpanalyzer.util.ByteToBits.byte2bits;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sovereign
 */
public class PhdInputStream {
  
  private static final String PHD_HEADER = "portable heap dump";
  public static final byte WORD_SIZE_64_BIT = 8;
  public static final byte WORD_SIZE_32_BIT = 4;
  private boolean headerRead = false;
  private Class<? extends Number> wordType = Integer.TYPE;
  private boolean eof = false;
  private boolean objectsHashed = false;
  private Class<? extends PhdRecord> nextRecordType;
  private PhdTag nextRecordTag;
  private RandomAccessFile file;
  private boolean moreObjects;
  
  public PhdInputStream(File file) throws FileNotFoundException {
    this.file = new RandomAccessFile(file, "r");
  }
  
  public PhdHeader readHeader() throws IncorrectFormatException, IOException {
    String headerString = file.readUTF();
    if (!headerString.equals(PHD_HEADER))
      throw new IncorrectFormatException();
    
    PhdHeader header = new PhdHeader();
    int phdVersion = file.readInt();
    int flags = file.readInt();
    boolean bit64 = (flags & 1) == 1;
    boolean hashed = (flags & 2) == 2;
    this.objectsHashed = hashed;
    boolean ibmJ9 = (flags & 4) == 4;
    
    byte recordTag = file.readByte();
    if (recordTag != 1)
      throw new IncorrectFormatException();
    
    String jvmVersion = null;
    recordTag = file.readByte();
    
    while (recordTag != 2) {
      
      if (recordTag == 4) {
        jvmVersion = file.readUTF();
      } else {
        while (file.readByte() != recordTag) {};
      }
      
      recordTag = file.readByte();
    }
    
    header.setPhdVersion(phdVersion);
    header.set64Bit(bit64);
    header.setHashed(hashed);
    header.setIbmJ9(ibmJ9);
    header.setJvmVersion(jvmVersion);
    
    this.headerRead = true;
    this.wordType = bit64? Long.TYPE : Integer.TYPE;
    
    if (file.readByte() != 2)
      Logger.getLogger(PhdInputStream.class.getName()).warning("no \"start of dump\" tag");
    
    moreObjects = true;
    
    return header;
  }
  
  public Number readWord() throws IOException {
    // System.out.println("at " + file.getFilePointer() + ", reading word (" + this.wordType.getSimpleName() + ")");
    return readValue(this.wordType);
  }
  
  public Number readValue(Class type) throws IOException {
    System.out.println(
      "at "
        + file.getFilePointer()
        + " (0x"
        + Long.toHexString(file.getFilePointer())
        + "), reading value ("
        + type.getSimpleName()
        + ")"
    );
    if (type.equals(Long.TYPE)) {
      System.out.println("long");
      return file.readLong();
    } else if (type.equals(Integer.TYPE)) {
      System.out.println("int");
      return file.readInt();
    } else if (type.equals(Short.TYPE)) {
      System.out.println("short");
      return file.readShort();
    } else if (type.equals(Byte.TYPE)) {
      System.out.println("byte");
      return file.readByte();
    }
    
    throw new IOException("Unknown data type");
  }
  
  public boolean objectsHashed() {
    return this.objectsHashed;
  }
  
  protected void checkNextObject() throws IOException, UnknownTagException {
    nextRecordTag = new PhdTag(file.readByte());
    
    if (nextRecordTag.toByte() == (byte)3) {
      moreObjects = false;
      return;
    }
    
    nextRecordType = nextRecordTag.toRecordType();
    
    if (nextRecordType == null)
      moreObjects = false;
  }
  
  public boolean hasMoreObjects() {
    return moreObjects;
  }
  
  public PhdRecord nextRecord() throws IOException, NoMoreRecordsException, UnknownTagException {
    // System.out.println("at " + file.getFilePointer() + "...");
    
    if (!moreObjects)
      throw new NoMoreRecordsException();
    
    PhdRecord record;
    
    if (nextRecordType != null) {
      record = PhdRecord.getFrom(this, nextRecordTag, nextRecordType);
    } else {
      record = PhdRecord.getFrom(this);
    }
    
    System.out.println(
      "position: "
        + file.getFilePointer()
        + " (0x"
        + Long.toHexString(file.getFilePointer())
        + ")"
    );
    
    checkNextObject();
    return record;
  }
  
  public void seek(long pos) throws IOException {
    file.seek(pos);
    nextRecordTag = null;
    nextRecordType = null;
    
    System.out.println("moved to " + pos + "...");
  }
  
  public PhdTag readTag() throws IOException {
    return new PhdTag(file.readByte());
  }
  
  public byte readByte() throws IOException {
    return file.readByte();
  }
  
  public char readChar() throws IOException {
    return file.readChar();
  }
  
  public short readShort() throws IOException {
    return file.readShort();
  }
  
  public int readInt() throws IOException {
    return file.readInt();
  }
  
  public long readLong() throws IOException {
    return file.readLong();
  }
  
  public String readUTF() throws IOException {
    return file.readUTF();
  }
  
  public Class<? extends Number> getWordType() {
    return this.wordType;
  }
  
}
