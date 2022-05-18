/*
 */
package com.example.springapp.dumpanalyzer.heap.phd.record;

/**
 *
 * @author Sovereign
 */
public class PhdTag {
  
  private byte tag;
  
  public PhdTag(byte tag) {
    this.tag = tag;
  }
  
  public byte toByte() {
    return this.tag;
  }
  
  public Class<? extends PhdRecord> toRecordType() throws UnknownTagException {
    Class<? extends PhdRecord> result = null;
    System.out.println("resolving tag: " + tag + " (0x" + Long.toHexString(tag) + ")");
    
    if (tag == 3) {
      System.out.println("end");
      if (true)
        throw new UnknownTagException(Byte.toString(tag));
      return null;
    } else if ((tag & 0x80) == 0x80) {
      // short object record
      
      return PhdShortObjectRecord.class;
      
    } else if (((tag & 0x40) == 0x40) && (tag > 0)) {
      // medium object record
      
//      if (true)
//        throw new UnknownTagException(Byte.toString(tag));
      return PhdMediumObjectRecord.class;
      
    } else if ((tag & 0x20) == 0x20) {
      // primitive array record
      
      return PhdPrimitiveArrayRecord.class;
      
    } else if (tag == 4) {
      // long object record
      
      return PhdLongObjectRecord.class;
      
    } else if (tag == 5) {
      // object array record
      
      throw new Error("end");
      
    } else if (tag == 6) {
      // class record
      
      if (true)
        throw new UnknownTagException(Byte.toString(tag));
      return PhdClassRecord.class;
      
    } else if (tag == 7) {
      // long primitive array record
      
      throw new Error("end");
      
    } else if (tag == 8) {
      // object array record (rev)
      
      throw new Error("end");
      
    }
    
    throw new UnknownTagException(Byte.toString(tag));
  }
  
}
