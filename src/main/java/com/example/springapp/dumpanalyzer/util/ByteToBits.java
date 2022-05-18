/*
 */
package com.example.springapp.dumpanalyzer.util;

/**
 *
 * @author Sovereign
 */
public abstract class ByteToBits {
  
  public static String byte2bits(byte b) {
    String out = "";
    
    for (byte bitNum = 0; bitNum < 8; bitNum += 1) {
      out = ((b & (1 << bitNum)) != 0? "1" : "0") + out;
    }
    
    return out;
  }
  
}
