/*
 */
package com.example.springapp.dumpanalyzer.data;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 *
 * @author Sovereign
 */
public class ManagedFile {
  
  private final String name;
  private final String type;
  private final Map<String, Future<Void>> processingState;
  
  public ManagedFile(String name, String type) {
    this.name = name;
    this.type = type;
    this.processingState = new ConcurrentHashMap<>();
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getType() {
    return this.type;
  }
  
  public void saveState(String state, Future<Void> processing) {
    processingState.put(state, processing);
  }
  
  public void removeState(String state) {
    processingState.remove(state);
  }
  
  public Future<Void> getState(String state) {
    return processingState.get(state);
  }
  
  public boolean hasSavedState(String state) {
    return processingState.containsKey(state);
  }
  
  public Map<String, Future<Void>> getAllStates() {
    return Collections.unmodifiableMap(processingState);
  }
  
  @Override
  public boolean equals(Object other) {
    if (Objects.isNull(other))
      return false;
    
    if (!(other instanceof ManagedFile))
      return false;
    
    return (
      getName().equals(
        ((ManagedFile)other).getName()
      )
      && getType().equals(
        ((ManagedFile)other).getType()
      )
    );
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + Objects.hashCode(this.name);
    hash = 59 * hash + Objects.hashCode(this.type);
    return hash;
  }
  
}
