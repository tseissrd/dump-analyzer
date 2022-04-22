/*
 */
package com.example.springapp.cpuloader.util;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Random;

/**
 *
 * @author Sovereign
 */
public class PrimeGenerator {
  
  private final Random random;
  private final int bitLength;
  
  public PrimeGenerator(int bitLength) {
    random = new Random(Instant.now().getEpochSecond());
    this.bitLength = bitLength;
  }
  
  public BigInteger next() {
    return BigInteger.probablePrime(bitLength, random);
  }
  
}