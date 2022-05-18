/*
 */
package com.example.springapp.dumpanalyzer.heap.phd;

import static com.example.springapp.dumpanalyzer.util.BooleanTranslate.b2s;


/**
 *
 * @author Sovereign
 */
public class PhdHeader {
  
  private int phdVersion;
  private boolean bit64;
  private boolean hashed;
  private boolean ibmJ9;
  private String jvmVersion;
  
  public PhdHeader() {}
  
  public void setPhdVersion(int phdVersion) {
    this.phdVersion = phdVersion;
  }
  
  public void set64Bit(boolean bit64) {
    this.bit64 = bit64;
  }
  
  public void setHashed(boolean hashed) {
    this.hashed = hashed;
  }
  
  public void setIbmJ9(boolean ibmJ9) {
    this.ibmJ9 = ibmJ9;
  }
  
  public void setJvmVersion(String jvmVersion) {
    this.jvmVersion = jvmVersion;
  }
  
  public boolean get64Bit() {
    return bit64;
  }
  
  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    
    out.append("PHD version: ")
      .append(phdVersion)
      .append("\n64bit: ")
      .append(bit64)
      .append("\nHashed: ")
      .append(hashed)
      .append("\nIBM J9: ")
      .append(ibmJ9)
      .append("\nJVM version: ")
      .append(jvmVersion);
    
    return out.toString();
  }
  
}
