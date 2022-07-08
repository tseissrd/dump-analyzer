/*
 */
package com.example.springapp.dumpanalyzer;

import com.example.springapp.dumpanalyzer.data.json.JsonOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

/**
 *
 * @author Sovereign
 */
public class Test {
  
  public static void main(String[] args) throws Throwable {
    JsonOutputStream jsonOut = new JsonOutputStream(System.out);
    jsonOut.test_convertObject(123);
    jsonOut.test_convertObject((double)543.123);
  }
  
}
