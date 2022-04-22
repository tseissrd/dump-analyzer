/*
 */
package com.example.springapp.cpuloader.threads;

import com.example.springapp.cpuloader.util.PrimeGenerator;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

/**
 *
 * @author Sovereign
 */
@Component
public class ThreadDestroyer {
  
  public enum Strategy {
    PRIME(() -> {
      PrimeGenerator generator = new PrimeGenerator(2048);
      BigInteger prime = generator.next();
      
      int i = 0;
      while (i < 1) {
        if (Thread.interrupted())
          return;
        
        prime = prime.nextProbablePrime();
      }
    }),
    ARRAY_COPY(() -> {
      String[] array = new String[(int)Math.floor(Math.pow(2, 16))];
      Random random = new Random(Instant.now().getEpochSecond());
      
      int i = 0;
      while (i < 1) {
        if (Thread.interrupted())
          return;
        
        array = Arrays.copyOf(array, array.length);
      }
    }),
    CRASH_HEAP(() -> {
      List list = new ArrayList();
      
      int i = 0;
      while (i < 1) {
        if (Thread.interrupted())
          return;
        
        list.add(null);
      }
    }),
    DEAD_LOCK(() -> {
      Random random = new Random(Instant.now().getEpochSecond());
      final int numberOfThreads = random.nextInt(1, 9);
      Thread[] threads = new Thread[numberOfThreads];
      //final List<Object> objects = new ArrayList<>();
      final List<Lock> resources = new ArrayList<>();
      
      for (int i = 0; i < numberOfThreads; i += 1)
        resources.add(new ReentrantLock());
        //objects.add(new Object());
      
      for (int i = 0; i < numberOfThreads; i += 1) {
        final int threadNumber = i;
        
        Runnable threadTask = () -> {
          int nextThreadNumber = threadNumber + 1;
          
          if (threadNumber + 1 == numberOfThreads)
            nextThreadNumber = 0;
          
          try {
            // synchronized(objects.get(threadNumber)) {
            resources.get(threadNumber).lockInterruptibly();
            try {
              Thread.sleep(200);

              // synchronized(objects.get(nextThreadNumber)) {
              resources.get(nextThreadNumber).lockInterruptibly();
              resources.get(nextThreadNumber).unlock();
              return;
            } catch (Throwable ex) {
              resources.get(threadNumber).unlock();
            }
          } catch (InterruptedException ex) {
              return;
          }
        };
        
        threads[threadNumber] = new Thread(threadTask);
        threads[threadNumber].start();
      }
      
      try {
        for (int i = 0; i < numberOfThreads; i += 1)
          threads[i].join();
      } catch (InterruptedException ex) {
        for (int i = 0; i < numberOfThreads; i += 1)
          threads[i].interrupt();
        
        return;
      }
    });
    
    final private Runnable task;
    
    private Strategy(Runnable task) {
      this.task = task;
    }
    
    public Runnable getTask() {
      return this.task;
    }
  }
  
  private int threadsNumber;
  private List<Thread> threads;
  private Strategy strategy;
  
  public ThreadDestroyer() {
    threadsNumber = 0;
    threads = new ArrayList<>();
    this.strategy = Strategy.PRIME;
  }
  
  public void setStrategy(Strategy newStrategy) {
    strategy = newStrategy;
    reload();
  }
  
  public void reload() {
    load(0);
    load(threadsNumber);
  }
  
  public void load(int threadsNumber) {
    if (threadsNumber > this.threadsNumber) {
      for (int i = 0; i < threadsNumber - this.threadsNumber; i += 1) {
        Thread newThread = new Thread(strategy.getTask());
        newThread.start();
        
        threads.add(newThread);
      }
    } else if (threadsNumber < this.threadsNumber) {
      int numOfThreadsToRemove = this.threadsNumber - threadsNumber;
      
      for (int i = 0; i < numOfThreadsToRemove; i += 1) {
        Thread threadToStop = threads.get(threads.size() - 1 - i);
        threadToStop.interrupt();
      }
      
      if (threadsNumber == 0)
        threads = new ArrayList<>();
      else
        threads = threads.subList(0, threadsNumber - 1);
    }
      
    this.threadsNumber = threadsNumber;
  }
  
  public int getThreadsNumber() {
    return threadsNumber;
  }
  
  public Strategy getStrategy() {
    return strategy;
  }
  
}
