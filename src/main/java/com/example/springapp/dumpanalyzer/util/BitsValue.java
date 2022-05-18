/*
 */
package com.example.springapp.dumpanalyzer.util;

import static com.example.springapp.dumpanalyzer.util.ByteToBits.byte2bits;

/**
 *
 * @author Sovereign
 */
public abstract class BitsValue {
  
  public String ba2s(byte[] arr) {
    StringBuilder out = new StringBuilder();
    
    for (byte b: arr) {
      out.append((int)b)
        .append(" ");
    }
    return out.toString();
  }
  
  public static byte valueOf(byte source, byte bitsNum, byte bitOffset, boolean fromMSB) {
//    if (fromMSB) {
//      byte mask = (byte)0x80;
//
//      byte resultMask = (byte)(mask >> (bitsNum + bitOffset - 1));
//      if (bitOffset == 1)
//        resultMask = (byte)(resultMask ^ mask);
//      else if (bitOffset > 1)
//        resultMask = (byte)(resultMask ^ (mask >> (bitOffset - 1)));
//
//      return (byte)((source & resultMask) >>> (8 - bitOffset - bitsNum));
//    } else {
      byte mask = 1;

      for (byte i = 1; i < bitsNum; i += 1)
        mask = (byte)((mask << 1) | 1);
      
      byte technicalOffset;
      
      if (fromMSB)
        technicalOffset = (byte)(8 - bitOffset - bitsNum);
      else
        technicalOffset = bitOffset;
      
      return (byte)((source >>> technicalOffset) & mask);

//      return (byte)((source & (mask << bitOffset)) >>> bitOffset);
//    }
  }
  
}
