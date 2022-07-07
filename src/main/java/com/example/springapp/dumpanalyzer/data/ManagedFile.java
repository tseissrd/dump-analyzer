/*
 */
package com.example.springapp.dumpanalyzer.data;

import java.util.Objects;

/**
 *
 * @author Sovereign
 */
public class ManagedFile {
  
  private final String name;
  private final String type;
  
  public ManagedFile(String name, String type) {
    this.name = name;
    this.type = type;
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getType() {
    return this.type;
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
