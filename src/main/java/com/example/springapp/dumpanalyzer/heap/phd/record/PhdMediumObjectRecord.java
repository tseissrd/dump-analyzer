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
public class PhdMediumObjectRecord extends PhdObjectRecord {
  
  private Object referencesArray;

  @Override
  public Object getReferencesArray() {
    return referencesArray;
  }
  
  private PhdMediumObjectRecord() {
  }

  public static PhdMediumObjectRecord getFrom(PhdInputStream input, PhdTag tag)
  throws IOException, IncorrectFormatException {
    PhdMediumObjectRecord record = new PhdMediumObjectRecord();
    
    byte tagByte = tag.toByte();
    
    if (BitsValue.valueOf(tagByte, (byte)2,(byte)0, true) != (byte)1)
      throw new IncorrectFormatException("First two bits of a medium object record have to be 01");
    
    byte numberOfReferences = BitsValue.valueOf(tagByte, (byte)3, (byte)2, true);
//    if ((numberOfReferences < 4) || (numberOfReferences > 7))
//      throw new IncorrectFormatException("Medium object record has to contain 4-7 references, got " + numberOfReferences);
    if (numberOfReferences < 0)
      throw new IncorrectFormatException("Number of references has to be > 0, got " + numberOfReferences);
    
    Class gapType = null;
    
    try {
      gapType = FormatCodes.formatForCode(BitsValue.valueOf(tagByte, (byte)1, (byte)2, false));
      if ((!gapType.equals(Byte.TYPE)) && (!gapType.equals(Short.TYPE)))
        throw new IncorrectFormatException("Gap type is " + gapType.getSimpleName() + " for class PhdMediumObjectRecord.");
    } catch (UnknownTypeCodeException ex) {
      Logger.getLogger(PhdMediumObjectRecord.class.getName()).log(Level.SEVERE, null, ex);
    }

    Class referenceType = null;
    
    try {
      referenceType = FormatCodes.formatForCode(BitsValue.valueOf(tagByte, (byte)2, (byte)0, false));
    } catch (UnknownTypeCodeException ex) {
      Logger.getLogger(PhdMediumObjectRecord.class.getName()).log(Level.SEVERE, null, ex);
    }

    Number gap = input.readValue(gapType);
    Number classAddress = input.readWord();
    
    short hashcode;
    
    if (input.objectsHashed())
      hashcode = input.readShort();

    record.referencesArray = Array.newInstance(referenceType, numberOfReferences);
    for (int refNum = 0; refNum < numberOfReferences; refNum += 1)
      Array.set(record.referencesArray, refNum, input.readValue(referenceType));
    
    System.out.println("reference format: " + referenceType.getName());
    if (numberOfReferences > 0)
      System.out.println("reference array class: " + record.referencesArray.getClass().getName());
    System.out.println("reference array length: " + numberOfReferences);
    System.out.println("class address: " + classAddress);
    System.out.println("gap: " + gap);
    System.out.println("");

    return record;
  }
  
}
