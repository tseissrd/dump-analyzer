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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sovereign
 */
public class PhdShortObjectRecord extends PhdObjectRecord {
  
  private Object referencesArray;

  @Override
  public Object getReferencesArray() {
    return referencesArray;
  }
  
  private PhdShortObjectRecord() {
  }

  public static PhdShortObjectRecord getFrom(PhdInputStream input, PhdTag tag) throws IOException, IncorrectFormatException {
    PhdShortObjectRecord record = new PhdShortObjectRecord();
    
    byte tagByte = tag.toByte();
    
    if (BitsValue.valueOf(tagByte, (byte)1, (byte)0, true) != 1)
      throw new IncorrectFormatException("First bit in a short object record must be set");
      
    byte classCacheIndex = BitsValue.valueOf(tagByte, (byte)2, (byte)1, true);
    byte numberOfReferences = BitsValue.valueOf(tagByte, (byte)2, (byte)3, true);
    
    if (numberOfReferences < 0)
      throw new IncorrectFormatException("Number of references has to be > 0, got " + numberOfReferences);
    
    if (numberOfReferences > 3)
      throw new IncorrectFormatException("Short object record has to contain 0-3 references, got " + numberOfReferences);
    
    Class gapType = null;
    
    try {
      gapType = FormatCodes.formatForCode(BitsValue.valueOf(tagByte, (byte)1, (byte)5, true));
      if ((!gapType.equals(Byte.TYPE)) && (!gapType.equals(Short.TYPE)))
        throw new IncorrectFormatException("Gap type is " + gapType.getSimpleName() + " for class PhdShortObjectRecord.");
    } catch (UnknownTypeCodeException ex) {
      throw new IncorrectFormatException(ex);
    }
    
    Class referenceType = null;
    
    try {
      referenceType = FormatCodes.formatForCode(BitsValue.valueOf(tagByte, (byte)2, (byte)0, false));
    } catch (UnknownTypeCodeException ex) {
      throw new IncorrectFormatException(ex);
    }
    
    Number gap = input.readValue(gapType);
    
    short hashcode;
    
    if (input.objectsHashed())
      hashcode = input.readShort();
    
    if (numberOfReferences > 0) {
      record.referencesArray = Array.newInstance(referenceType, numberOfReferences);
      
      for (int refNum = 0; refNum < numberOfReferences; refNum += 1)
        Array.set(record.referencesArray, refNum, input.readValue(referenceType));
    }
    
    System.out.println("reference format: " + referenceType.getName());
    if (numberOfReferences > 0)
      System.out.println("reference array class: " + record.referencesArray.getClass().getName());
    System.out.println("reference array length: " + numberOfReferences);
    // System.out.println("reference array component class: " + record.referencesArray.getClass().componentType().getName());
    System.out.println("reference array component class: " + referenceType.getName());
    System.out.println("class cache index: " + classCacheIndex);
    System.out.println("gap: " + gap);
    System.out.println("");

    return record;
  }
  
}
