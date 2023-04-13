import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Problem2 {

    // Define constants to use in the program
    private static final int NUM_SENSORS = 8;
    private static final int NUM_READINGS_PER_HOUR = 60;
    private static final int MAX_TEMPERATURE = 70;
    private static final int MIN_TEMPERATURE = -100;

    // Create a concurrent hash map to store temperature readings for each minute
    private static final ConcurrentHashMap<Integer, AtomicInteger> temperatureReadings = new ConcurrentHashMap<>();
    
    // Create a cyclic barrier that will trigger the hourly report once all sensors have reported in
    private static final CyclicBarrier hourlyBarrier = new CyclicBarrier(NUM_SENSORS, new HourlyReporter());

    // Define a class to be used for the hourly reporting
    private static class HourlyReporter implements Runnable {
        @Override
        public void run() {
            // Create arrays to store the highest and lowest temperatures for each minute
            List<Integer> highestTemperatures = new ArrayList<>();
            List<Integer> lowestTemperatures = new ArrayList<>();
            // Create variables to track the largest temperature difference and when it occurred
            int largestTemperatureDifference = 0;
            int largestTemperatureDifferenceStart = 0;
            int largestTemperatureDifferenceEnd = 0;

            // Iterate through each minute of the hour
            for (int i = 0; i < NUM_READINGS_PER_HOUR; i++) {
                // Create a list to store the temperature readings for each sensor for the current minute
                List<Integer> temperatures = new ArrayList<>();
                for (int j = 0; j < NUM_SENSORS; j++) {
                    // Get the temperature reading for the current sensor for the current minute
                    temperatures.add(temperatureReadings.get(i * NUM_SENSORS + j).get());
                }
                // Find the highest and lowest temperatures for the current minute
                int max = Collections.max(temperatures);
                int min = Collections.min(temperatures);
                // Add the highest and lowest temperatures to the arrays
                highestTemperatures.add(max);
                lowestTemperatures.add(min);
                // Find the difference between the highest and lowest temperatures for the current minute
                int temperatureDifference = max - min;
                // Update the variables tracking the largest temperature difference if necessary
                if (temperatureDifference > largestTemperatureDifference) {
                    largestTemperatureDifference = temperatureDifference;
                    largestTemperatureDifferenceStart = i;
                    largestTemperatureDifferenceEnd = i;
                }
            }

            // Print out the top 5 unique highest temperatures
            System.out.println("Top 5 unique highest temperatures: " + getTopNUnique(highestTemperatures, 5));
            // Print out the top 5 unique lowest temperatures
            System.out.println("Top 5 unique lowest temperatures: " + getTopNUnique(lowestTemperatures, 5, false));
            // Print out the largest temperature difference and when it occurred
            System.out.println("Largest temperature difference (" + largestTemperatureDifference + "F) from minute " + largestTemperatureDifferenceStart +
                    " to minute " + largestTemperatureDifferenceEnd);

            // Exit the program once the hourly report is complete
            System.exit(0); 
        }

        // Define a helper method to get the top n unique temperatures from a list
        private List<Integer> getTopNUnique(List<Integer> temperatures, int n) {
            return getTopNUnique(temperatures, n, true);
        }

        // Define a helper method to get the top n unique temperatures from a list, either highest
        private List<Integer> getTopNUnique(List<Integer> temperatures, int n, boolean highest) {
           
            Set<Integer> uniqueTemperatures = new HashSet<>(temperatures);
            List<Integer> sortedTemperatures = new ArrayList<>(uniqueTemperatures);
           
            Collections.sort(sortedTemperatures);
            if (highest) {
                Collections.reverse(sortedTemperatures);
            }
            return sortedTemperatures.subList(0, Math.min(n, sortedTemperatures.size()));
        }
     }

    // Driver Method
    public static void main(String[] args) {

        // Create a fixed thread pool executor with the number of threads equal to the number of sensors
        ExecutorService executor = Executors.newFixedThreadPool(NUM_SENSORS);
    
        // Iterate through each sensor and submit a task to the executor
        for (int i = 0; i < NUM_SENSORS; i++) {
            // Define a final variable for the current sensor id to be used in the task
            final int sensorId = i;
            // Submit a task to the executor to generate temperature readings for the current sensor for the hour
            executor.submit(() -> {
                // Create a new Random object to generate random temperature readings
                Random random = new Random();
                // Iterate through each minute of the hour and generate a temperature reading for the current sensor for that minute
                for (int j = 0; j < NUM_READINGS_PER_HOUR; j++) {
                    // Generate a random temperature between the minimum and maximum temperatures
                    int temperature = random.nextInt(MAX_TEMPERATURE - MIN_TEMPERATURE + 1) + MIN_TEMPERATURE;
                    // Add the temperature reading to the concurrent hash map for the current sensor and minute
                    temperatureReadings.put(sensorId * NUM_READINGS_PER_HOUR + j, new AtomicInteger(temperature));
                }
                // Wait for all sensors to finish generating temperature readings for the current minute
                try {
                    hourlyBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
        }
        // Shut down the executor once all tasks have been submitted
        executor.shutdown();
    }
}

