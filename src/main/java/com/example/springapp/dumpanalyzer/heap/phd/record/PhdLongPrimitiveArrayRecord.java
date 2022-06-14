/*
 */
package com.example.springapp.dumpanalyzer.heap.phd.record;

import com.example.springapp.dumpanalyzer.heap.phd.IncorrectFormatException;
import com.example.springapp.dumpanalyzer.heap.phd.PhdInputStream;
import static com.example.springapp.dumpanalyzer.heap.phd.record.ComponentTypeCodes.typeForCode;
import static com.example.springapp.dumpanalyzer.heap.phd.record.FormatCodes.formatForCode;
import com.example.springapp.dumpanalyzer.util.BitsValue;
import com.example.springapp.dumpanalyzer.util.ByteToBits;
import static com.example.springapp.dumpanalyzer.util.ByteToBits.byte2bits;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sovereign
 */
public class PhdLongPrimitiveArrayRecord extends PhdArrayRecord {
  
  private Class componentType;
  private Number length;
  private Number hashcode;
  
  public static PhdLongPrimitiveArrayRecord getFrom(PhdInputStream input, PhdTag tag) throws IOException, IncorrectFormatException {
    
    PhdLongPrimitiveArrayRecord record = new PhdLongPrimitiveArrayRecord();
    
    byte tagByte = tag.toByte();
    
    if (tagByte != 7)
      throw new IncorrectFormatException("Tag value for a long primitive array record has to be 7.");
    
    byte flags = input.readByte();
    
    try {
      record.componentType = typeForCode(BitsValue.valueOf(flags, (byte)3, (byte)0, true));
    } catch (UnknownTypeCodeException ex) {
      throw new IncorrectFormatException(ex);
    }
    
    byte arrayLengthFormatCode = BitsValue.valueOf(flags, (byte)1, (byte)3, true);
    boolean moved = BitsValue.valueOf(flags, (byte)1, (byte)1, false) == 1;
    boolean hashed = BitsValue.valueOf(flags, (byte)1, (byte)0, false) == 1;
    
    Class arrayLengthFormat = null; // also gap from previous object
    
    if (arrayLengthFormatCode == (byte)1)
      arrayLengthFormat = input.getWordType();
    else if (arrayLengthFormatCode == (byte)0)
      arrayLengthFormat = Byte.TYPE;
    else
      throw new IncorrectFormatException("Unknown long array length type code: " + arrayLengthFormatCode);
    
    Number gapFromPreviousObject = input.readValue(arrayLengthFormat);
    record.setGap(gapFromPreviousObject);
    record.length = input.readValue(arrayLengthFormat);
    
    if (record.length.longValue() < 0)
      throw new IncorrectFormatException("Array length has to be > 0, got " + record.length.longValue());
    
    if (input.objectsHashed())
      record.hashcode = input.readShort();
    else if (moved)
      record.hashcode = input.readInt();
    
    // int instanceSize = input.readInt(); // unsigned
    
    System.out.println(byte2bits(tagByte));
    System.out.println("array component type: " + record.componentType.getName());
    System.out.println("array length format: " + record.length.getClass().getName());
    System.out.println("array length: " + record.length);
    System.out.println("gap: " + gapFromPreviousObject);
    if (input.objectsHashed() || moved)
      System.out.println(record.hashcode);
    // System.out.println("instance size: " + instanceSize);
    System.out.println("");
    
    return record;
  }
  
  private PhdLongPrimitiveArrayRecord() {
  }
  
}
