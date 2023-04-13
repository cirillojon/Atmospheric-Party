# COP4520 Assignment 3


## Problem 1

In this code, 4 servants concurrently perform either: 
1. Adding a present to a chain
2. Removing a present and writing a "Thank you" note
3. Checking for a present with a specific tag

This code uses AtomicIntegers, the ReentrantLock Java Class, and the ExecutorService Java Class to manage shared resources, synchronize threads, and ensure progress. These components contribute to the efficiency, correctness, and progress guarantee of the solution, allowing the simulation to run smoothly and identify any discrepancies between the number of presents and "Thank you" notes.


## Problem 2

This code implements an Atmospheric Temperature Reading Module for the Mars Rover, utilizing eight threads for concurrent temperature data collection from multiple sensors. This code utilizes a ExecutorSerivce, ConcurrentHashMap and CyclicBarrier. These are used to ensure efficient shared resource management and thread synchronization. The program ensures proper temperature data storage and report generation once all readings are collected. ExecutorService guarantees progress by efficiently managing threads and resources, while ConcurrentHashMap and CyclicBarrier prevent unnecessary thread blocking.


## To run
1. Clone the repository
2. Navigate to the directory
3. Run the following command: `javac Problem1.java`
4. Run the following command: `java Problem1`
5. Run the following command: `javac Problem2.java`
6. Run the following command: `java Problem2`
