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
public class PhdClassRecord extends PhdObjectRecord {
  
  private Object referencesArray;

  @Override
  public Object getReferencesArray() {
    return referencesArray;
  }
  
  private PhdClassRecord() {
  }

  public static PhdClassRecord getFrom(PhdInputStream input, PhdTag tag)
  throws IOException, IncorrectFormatException {
    PhdClassRecord record = new PhdClassRecord();
    
    if (tag.toByte() != (byte)6)
      throw new IncorrectFormatException("Tag value for a class record has to be 6.");
    
    byte flags = input.readByte();

    Class gapType = null;
    try {
      gapType = formatForCode(BitsValue.valueOf(flags, (byte)2, (byte)0, true));
    } catch (UnknownTypeCodeException ex) {
      throw new IncorrectFormatException(ex);
    }

    Class referenceType = null;
    try {
      referenceType = formatForCode(BitsValue.valueOf(flags, (byte)2, (byte)2, true));
    } catch (UnknownTypeCodeException ex) {
      throw new IncorrectFormatException(ex);
    }

    boolean moved = BitsValue.valueOf(flags, (byte)1, (byte)4, true) == 1;

    System.out.println("tag: " + tag);
    System.out.println("moved: " + moved);

    Number gap = input.readValue(gapType);

    int instanceSize = input.readInt();

    Number hashcode = null;

    if (input.objectsHashed())
      hashcode = input.readShort();
    else if (moved)
      hashcode = input.readInt();

    Number superclassAddress = input.readWord();
    
    System.out.println("instance size: " + instanceSize);
    System.out.println("reference format: " + referenceType.getName());
    System.out.println("superclass address: " + superclassAddress);
    System.out.println("gap type: " + gapType);
    System.out.println("gap: " + gap);
    
    String className = input.readUTF();
    
    int numberOfReferences = input.readInt();

    record.referencesArray = Array.newInstance(referenceType, numberOfReferences);

    System.out.println("reference array class: " + record.referencesArray.getClass().getName());
    System.out.println("reference array length: " + numberOfReferences);
    System.out.println("class name: " + className);
    System.out.println("");

    for (int refNum = 0; refNum < numberOfReferences; refNum += 1)
      Array.set(record.referencesArray, refNum, input.readValue(referenceType));

    return record;
  }
  
}
