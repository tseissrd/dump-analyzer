/*
 */
package com.example.springapp.dumpanalyzer.heap.phd.record;

/**
 *
 * @author Sovereign
 */
public abstract class FormatCodes {
  
  public static Class formatForCode(byte sizeCode) throws UnknownTypeCodeException {
    System.out.println(sizeCode);
    if (sizeCode == 0)
      return Byte.TYPE;
    else if (sizeCode == 1)
      return Short.TYPE;
    else if (sizeCode == 2)
      return Integer.TYPE;
    else if (sizeCode == 3)
      return Long.TYPE;
    
    throw new UnknownTypeCodeException(String.valueOf(sizeCode));
  }
  
}
