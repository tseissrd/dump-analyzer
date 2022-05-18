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
public class PhdPrimitiveArrayRecord extends PhdArrayRecord {
  
  private Class componentType;
  private Number length;
  private short hashcode;
  
  public static PhdPrimitiveArrayRecord getFrom(PhdInputStream input, PhdTag tag) throws IOException, IncorrectFormatException {
    
    PhdPrimitiveArrayRecord record = new PhdPrimitiveArrayRecord();
    
    byte tagByte = tag.toByte();
    
    try {
      record.componentType = typeForCode(BitsValue.valueOf(tagByte, (byte)3, (byte)3, true));
    } catch (UnknownTypeCodeException ex) {
      Logger.getLogger(PhdPrimitiveArrayRecord.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    byte arrayLengthFormatCode = BitsValue.valueOf(tagByte, (byte)2, (byte)0, false);
    
    Class arrayLengthFormat = null; // also gap from previous object
    
    try {
      arrayLengthFormat = formatForCode(arrayLengthFormatCode);
    } catch (UnknownTypeCodeException ex) {
      Logger.getLogger(PhdPrimitiveArrayRecord.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    Number gapFromPreviousObject = input.readValue(arrayLengthFormat);
    record.setGap(gapFromPreviousObject);
    record.length = input.readValue(arrayLengthFormat);
    
    if (record.length.longValue() < 0)
      throw new IncorrectFormatException("Array length has to be > 0, got " + record.length.longValue());
    
    if (input.objectsHashed())
      record.hashcode = input.readShort();
    
    System.out.println(byte2bits(tagByte));
    System.out.println("array component type: " + record.componentType.getName());
    System.out.println("array length format: " + record.length.getClass().getName());
    System.out.println("array length: " + record.length);
    System.out.println("gap: " + gapFromPreviousObject);
    if (input.objectsHashed())
      System.out.println(record.hashcode);
    System.out.println("");
    
    return record;
  }
  
  private PhdPrimitiveArrayRecord() {
  }
  
}
