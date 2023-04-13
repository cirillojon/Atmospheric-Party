import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Present {
    int tag;

    public Present(int tag) {
        this.tag = tag;
    }
}

public class Problem1 {
    private static final int NUM_PRESENTS = 500000;
    private static final int NUM_SERVANTS = 4;
    private static final int MAX_ITERATIONS_PER_SERVANT = 10000;

    public static void main(String[] args) throws InterruptedException {
        // Record the start time of the program
        long startTime = System.currentTimeMillis();

        // Create an empty linked list to store the presents
        LinkedList<Present> presentList = new LinkedList<>();

        // Use AtomicInteger to keep track of the number of "Thank you" notes written
        AtomicInteger thankYouNotesWritten = new AtomicInteger(0);

        // Use AtomicInteger to keep track of the number of present tags checked
        AtomicInteger checkedTags = new AtomicInteger(0);

        // Create a thread pool with a fixed number of threads (servants)
        ExecutorService executor = Executors.newFixedThreadPool(NUM_SERVANTS);

        // Create a new instance of Random for generating random numbers
        Random random = new Random();

        // Create a list of present tags from 1 to NUM_PRESENTS and shuffle them
        List<Integer> shuffledTags = IntStream.rangeClosed(1, NUM_PRESENTS).boxed().collect(Collectors.toList());
        Collections.shuffle(shuffledTags);

        // Create a new ReentrantLock to manage access to the presentList
        Lock presentLock = new ReentrantLock();

        // Loop through each servant and submit a task to the thread pool
        for (int i = 0; i < NUM_SERVANTS; i++) {
            //int servantIndex = i;
            executor.submit(() -> {
                int iteration = 0;
                // Loop through a maximum number of iterations per servant while the shuffledTags or presentList are not empty
                while (iteration < MAX_ITERATIONS_PER_SERVANT && (!shuffledTags.isEmpty() || !presentList.isEmpty())) {
                    // Generate a random action (0 = add present, 1 = remove present and write a "Thank you" note, 2 = check if present tag exists)
                    int action = random.nextInt(3);
                    if (action == 0) {
                        presentLock.lock();
                        try {
                            // If there are still shuffled tags remaining, add a present to the presentList with the next tag in the shuffledTags list
                            if (!shuffledTags.isEmpty()) {
                                int tag = shuffledTags.remove(0);
                                int idx = 0;
                                while (idx < presentList.size() && presentList.get(idx).tag < tag) {
                                    idx++;
                                }
                                presentList.add(idx, new Present(tag));
                                //System.out.println("Servant " + servantIndex + " added present with tag " + tag);
                            }
                        } finally {
                            presentLock.unlock();
                        }
                    } else if (action == 1) {
                        presentLock.lock();
                        try {
                            // If the presentList is not empty, remove the first present from the presentList and increment the thankYouNotesWritten counter
                            if (!presentList.isEmpty()) {
                                Present removedPresent = presentList.removeFirst();
                                thankYouNotesWritten.incrementAndGet();
                                //System.out.println("Servant " + servantIndex + " removed present with tag " + removedPresent.tag + " and wrote a "Thank you" note");
                            }
                        } finally {
                            presentLock.unlock();
                        }
                    } else {
                        // Generate a random present tag to check for
                        int randomTag = random.nextInt(NUM_PRESENTS) + 1;
                        presentLock.lock();
                        try {
                            // Check if the presentList contains a present with the random tag
                            boolean contains = presentList.stream().anyMatch(p -> p.tag == randomTag);
                            if (contains) {
                                checkedTags.incrementAndGet();
                                //System.out.println("Servant " + servantIndex + " found present with tag " + randomTag);
                            } else {
                            //System.out.println("Servant " + servantIndex + " did not find present with tag " + randomTag);
                            }
                        } finally {
                            presentLock.unlock();
                        }
                    }
                    try {
                        Thread.sleep(1); // Add a short delay to simulate the time taken by the servants to perform their tasks
                        iteration++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
        long endTime = System.currentTimeMillis();

        System.out.println("Time taken: " + (endTime - startTime) + " ms");
        System.out.println("Total presents processed: " + (NUM_PRESENTS - shuffledTags.size()));
        System.out.println("Total \"Thank you\" notes written: " + thankYouNotesWritten.get());
        System.out.println("Total checked tags: " + checkedTags.get());
    }
}
