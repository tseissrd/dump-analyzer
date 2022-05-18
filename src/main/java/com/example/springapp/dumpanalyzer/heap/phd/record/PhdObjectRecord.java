/*
 */
package com.example.springapp.dumpanalyzer.heap.phd.record;

/**
 *
 * @author Sovereign
 */
public abstract class PhdObjectRecord extends PhdRecord {
  
  private PhdTag tag;
  private int numberOfReferences;
  private Number classRecordAddress;
  
  protected void setTag(PhdTag tag) {
    this.tag = tag;
  }
  
  public PhdTag getTag() {
    return tag;
  }
  
  protected void setNumberOfReferences(int numberOfReferences) {
    this.numberOfReferences = numberOfReferences;
  }
  
  public int getNumberOfReferences() {
    return numberOfReferences;
  }
  
  protected void setClassRecordAddress(Number classRecordAddress) {
    this.classRecordAddress = classRecordAddress;
  }
  
  public Number getClassRecordAddress() {
    return classRecordAddress;
  }
  
  abstract public Object getReferencesArray();
  
}
