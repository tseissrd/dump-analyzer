/*
 */
package com.example.springapp.dumpanalyzer.heap.phd.record;

import com.example.springapp.dumpanalyzer.heap.phd.IncorrectFormatException;
import com.example.springapp.dumpanalyzer.heap.phd.NoMoreRecordsException;
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
public class PhdLongObjectRecord extends PhdObjectRecord {
  
  private Object referencesArray;

  @Override
  public Object getReferencesArray() {
    return referencesArray;
  }
  
  private PhdLongObjectRecord() {
  }

  public static PhdLongObjectRecord getFrom(PhdInputStream input, PhdTag tag) throws IOException, IncorrectFormatException {
    PhdLongObjectRecord record = new PhdLongObjectRecord();
    
    if (tag.toByte() != 4)
      throw new IncorrectFormatException("Tag value of a long object record has to be 4");
    
    byte flags = input.readByte();

    Class gapType = null;
    try {
      gapType = formatForCode(BitsValue.valueOf(flags, (byte)2, (byte)0, true));
    } catch (UnknownTypeCodeException ex) {
      Logger.getLogger(PhdLongObjectRecord.class.getName()).log(Level.SEVERE, null, ex);
    }

    Class referenceType = null;
    try {
      referenceType = formatForCode(BitsValue.valueOf(flags, (byte)2, (byte)2, true));
    } catch (UnknownTypeCodeException ex) {
      Logger.getLogger(PhdLongObjectRecord.class.getName()).log(Level.SEVERE, null, ex);
    }

    System.out.println("flags: " + byte2bits(flags));
    System.out.println("gap type: " + gapType);
    System.out.println(referenceType);

    boolean moved = BitsValue.valueOf(flags, (byte)1, (byte)1, false) == 1;
    boolean hashed = BitsValue.valueOf(flags, (byte)1, (byte)0, false) == 1;

    System.out.println("moved: " + moved);
    System.out.println("hashed: " + hashed);

    Number gap = input.readValue(gapType);
    record.setGap(gap);

    Number classAddress = input.readWord();
    record.setClassRecordAddress(classAddress);

    Number hashcode = null;

    if (input.objectsHashed())
      hashcode = input.readShort();
    else if (moved)
      hashcode = input.readInt();

    int numberOfReferences = input.readInt();
    
    if (numberOfReferences < 0)
      throw new IncorrectFormatException("Number of references has to be > 0, got " + numberOfReferences);
    
    if (!hashed && !moved && (numberOfReferences <= 7))
      throw new IncorrectFormatException(
        "Long object record has to contain either hashed or moved flags or >7 references, got "
          + numberOfReferences + " references and neither flag");

    record.referencesArray = Array.newInstance(referenceType, numberOfReferences);

    System.out.println("reference format: " + referenceType.getName());
    System.out.println("reference array class: " + record.referencesArray.getClass().getName());
    System.out.println("reference array length: " + numberOfReferences);
    System.out.println("class address: " + classAddress);
    System.out.println("gap: " + gap);
    System.out.println("");

    for (int refNum = 0; refNum < numberOfReferences; refNum += 1)
      Array.set(record.referencesArray, refNum, input.readValue(referenceType));

    return record;
  }
  
}
