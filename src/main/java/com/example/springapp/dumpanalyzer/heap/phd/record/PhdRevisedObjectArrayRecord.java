/*
 */
package com.example.springapp.dumpanalyzer.heap.phd.record;

import com.example.springapp.dumpanalyzer.heap.phd.IncorrectFormatException;
import com.example.springapp.dumpanalyzer.heap.phd.PhdInputStream;
import static com.example.springapp.dumpanalyzer.heap.phd.record.FormatCodes.formatForCode;
import com.example.springapp.dumpanalyzer.util.BitsValue;
import static com.example.springapp.dumpanalyzer.util.ByteToBits.byte2bits;
import java.io.IOException;
import java.lang.reflect.Array;

/**
 *
 * @author Sovereign
 */
public class PhdRevisedObjectArrayRecord extends PhdArrayRecord {
  
  private Class componentType;
  private Object referencesArray;
  private int length;
  
  public static PhdRevisedObjectArrayRecord getFrom(PhdInputStream input, PhdTag tag) throws IOException, IncorrectFormatException {
    
    PhdRevisedObjectArrayRecord record = new PhdRevisedObjectArrayRecord();
    
    byte tagByte = tag.toByte();
    
    if (tagByte != 8)
      throw new IncorrectFormatException("Tag value for revised object array record has to be 8");
    
    byte flags = input.readByte();
    
    Class gapFormat;
    Class referenceSize;
    
    try {
      gapFormat = formatForCode(
        BitsValue.valueOf(flags, (byte)2, (byte)0, true)
      );
      
      referenceSize = formatForCode(
        BitsValue.valueOf(flags, (byte)2, (byte)2, true)
      );
    } catch (UnknownTypeCodeException ex) {
      throw new IncorrectFormatException(ex);
    }
    
    boolean moved = BitsValue.valueOf(flags, (byte)1, (byte)1, false) == 1;
    boolean hashed = BitsValue.valueOf(flags, (byte)1, (byte)0, false) == 1;
    
    Number gap = input.readValue(gapFormat);
    Number classAddress = input.readWord();
    
    Number hashcode = null;
    
    if (moved)
      hashcode = input.readInt();
    else if (input.objectsHashed())
      hashcode = input.readShort();
    
    int referencesArrayLength = input.readInt();
    
    if (referencesArrayLength < 0)
      throw new IncorrectFormatException("References array length has to be >= 0, got " + referencesArrayLength);
    
    System.out.println(referenceSize.getSimpleName());
    record.referencesArray = Array.newInstance(referenceSize, referencesArrayLength);
    
    for (int referenceNum = 0; referenceNum < referencesArrayLength; referenceNum += 1)
      Array.set(record.referencesArray, referenceNum, input.readValue(referenceSize));
    
    record.length = input.readInt();
    
    if (record.length < 0)
      throw new IncorrectFormatException("Array length has to be >= 0, got " + record.length);
    
    // int instanceSize = input.readInt(); // unsigned
    
    System.out.println(byte2bits(tagByte));
    System.out.println("gap: " + gap);
    System.out.println("length: " + record.length);
    if (input.objectsHashed())
      System.out.println(hashcode);
    // System.out.println("instance size: " + instanceSize);
    System.out.println("");
    
    return record;
  }
  
  private PhdRevisedObjectArrayRecord() {
  }
  
}
