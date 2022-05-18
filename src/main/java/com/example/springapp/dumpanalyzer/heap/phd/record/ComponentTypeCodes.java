/*
 */
package com.example.springapp.dumpanalyzer.heap.phd.record;

/**
 *
 * @author Sovereign
 */
public abstract class ComponentTypeCodes {
  
  public static Class typeForCode(byte typeCode) throws UnknownTypeCodeException {
    if (typeCode == 0)
      return Boolean.TYPE;
    else if (typeCode == 1)
      return Character.TYPE;
    else if (typeCode == 2)
      return Float.TYPE;
    else if (typeCode == 3)
      return Double.TYPE;
    else if (typeCode == 4)
      return Byte.TYPE;
    else if (typeCode == 5)
      return Short.TYPE;
    else if (typeCode == 6)
      return Integer.TYPE;
    else if (typeCode == 7)
      return Long.TYPE;
    
    throw new UnknownTypeCodeException(String.valueOf(typeCode));
  }
  
}
