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
public class PhdObjectArrayRecord extends PhdArrayRecord {
  
  private Class componentType;
  private Object referencesArray;
  
  public static PhdObjectArrayRecord getFrom(PhdInputStream input, PhdTag tag) throws IOException, IncorrectFormatException {
    
    PhdObjectArrayRecord record = new PhdObjectArrayRecord();
    
    byte tagByte = tag.toByte();
    
    if (tagByte != 5)
      throw new IncorrectFormatException("Tag value for object array record has to be 5");
    
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
    
    record.referencesArray = Array.newInstance(referenceSize, referencesArrayLength);
    
    for (int referenceNum = 0; referenceNum < referencesArrayLength; referenceNum += 1)
      Array.set(record.referencesArray, referenceNum, input.readValue(referenceSize));
    
    // int instanceSize = input.readInt(); // unsigned
    
    System.out.println(byte2bits(tagByte));
    System.out.println("gap: " + gap);
    if (input.objectsHashed())
      System.out.println(hashcode);
    // System.out.println("instance size: " + instanceSize);
    System.out.println("");
    
    return record;
  }
  
  private PhdObjectArrayRecord() {
  }
  
}
