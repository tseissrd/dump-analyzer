/*
 */
package com.example.springapp.dumpanalyzer.util;

/**
 *
 * @author Sovereign
 */
public abstract class BooleanTranslate {
  
  public static String b2s(boolean bool) {
    return bool? "да" : "нет";
  }
  
}
