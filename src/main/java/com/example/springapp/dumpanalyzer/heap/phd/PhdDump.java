/*
 */
package com.example.springapp.dumpanalyzer.heap.phd;

import com.example.springapp.dumpanalyzer.heap.phd.record.PhdClassRecord;
import com.example.springapp.dumpanalyzer.heap.phd.record.PhdObjectRecord;
import com.example.springapp.dumpanalyzer.heap.phd.record.PhdRecord;
import com.example.springapp.dumpanalyzer.heap.phd.record.UnknownTagException;
import com.example.springapp.dumpanalyzer.util.BitsValue;
import static com.example.springapp.dumpanalyzer.util.ByteToBits.byte2bits;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sovereign
 */
public class PhdDump {
  
  private File path;
  private PhdHeader header;
  private PhdInputStream input;
  private FileInputStream fsInput;
  
  public PhdDump(File path) throws FileNotFoundException, IOException, IncorrectFormatException, NoMoreRecordsException {
    
    input = new PhdInputStream(path);
    // byte[] buf = new byte[256];
    
    header = input.readHeader();
    
    System.out.println(header.toString());
    System.out.println(byte2bits(BitsValue.valueOf((byte)0b10001011, (byte)4, (byte)1, false)));
//    if (true)
//      throw new Error();
    
    List<PhdRecord> records = new ArrayList<>();
    List<PhdClassRecord> classRecords = new ArrayList<>();
    int total = 0;
    PhdRecord record = null;
    
    while (input.hasMoreObjects()) {
    // for (int i = 0; i < 50; i += 1) {
      try {
        record = input.nextRecord();
        records.add(record);
        if (record.getClass().equals(PhdClassRecord.class))
          classRecords.add((PhdClassRecord)record);
          
        total += 1;
      } catch (NoMoreRecordsException ex) {
        // Logger.getLogger(PhdDump.class.getName()).log(Level.SEVERE, null, ex);
        throw ex;
      } catch (UnknownTagException ex) {
        Logger.getLogger(PhdDump.class.getName()).log(Level.SEVERE, null, ex);
        throw new IncorrectFormatException(ex);
      }
    }
    
    System.out.println("records total: " + total);
    System.out.println("class records: " + classRecords.size());
//    input.nextRecord();
//    System.out.println(classRecords.get(0));
    
    this.path = path;
  }
  
}
