/*
 */
package com.example.springapp.dumpanalyzer.heap.phd.record;

import com.example.springapp.dumpanalyzer.heap.phd.PhdInputStream;
import static com.example.springapp.dumpanalyzer.util.ByteToBits.byte2bits;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import static com.example.springapp.dumpanalyzer.heap.phd.record.FormatCodes.formatForCode;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sovereign
 */
public abstract class PhdRecord {
  
  private Number gap = null;
  
  public static PhdRecord getFrom(PhdInputStream input, PhdTag tag, Class<? extends PhdRecord> type)
    throws IOException {
    
    Method methodGetFrom = null;
    System.out.println("\n" + byte2bits(tag.toByte()) + " tag: " + tag.toByte() + ", type: " + type.getSimpleName());
    
    try {
      methodGetFrom = type.getMethod("getFrom", new Class[]{PhdInputStream.class, PhdTag.class});
    } catch (NoSuchMethodException ex) {
    } catch (SecurityException ex) {
    }
    
    if (methodGetFrom == null) {
      try {
        methodGetFrom = type.getMethod("getFrom", new Class[]{PhdInputStream.class});
      } catch (NoSuchMethodException ex) {
        Logger.getLogger(PhdRecord.class.getName()).log(Level.SEVERE, null, ex);
      } catch (SecurityException ex) {
        Logger.getLogger(PhdRecord.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    
    try {
      return (PhdRecord)methodGetFrom.invoke(null, input, tag);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(PhdRecord.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(PhdRecord.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InvocationTargetException ex) {
      Logger.getLogger(PhdRecord.class.getName()).log(Level.SEVERE, null, ex.getCause());
    }
    
    return null;
  }
  
  public static PhdRecord getFrom(PhdInputStream input) throws IOException, UnknownTagException {
    PhdTag tag = input.readTag();
    
    return getFrom(input, tag, tag.toRecordType());
  }
  
  public void setGap(Number gap) {
    this.gap = gap;
  }
  
  public Number getGap() {
    return this.gap;
  }
  
}
