import java.util.Arrays;

public class LargeSumExample {

    private static final int numberCount = 1_000_000_000;

    private static final int threadCount = 20;

    private static void benchmark(Runnable callable) {
        final long startTime = System.currentTimeMillis();
        try {
            callable.run();
        } finally {
            final long endTime = System.currentTimeMillis();
            final long elapsedTime = endTime - startTime;
            System.out.println("Elapsed time: " + elapsedTime + " ms");
        }
    }

    private static void showCpuInfo() {
        final int coreCount = Runtime.getRuntime().availableProcessors();
        System.out.println("The number of cores in the system is " + coreCount);
    }

    public static void main(String[] args) {
        showCpuInfo();

        final SumThread[] threads = new SumThread[threadCount];
        final int[] numbers = new int[numberCount];
        /* Populate the array with initial numbers. */
        Arrays.fill(numbers, 1);
        if (numberCount % threadCount != 0) {
            System.err.println("The final sum will be incorrect.");
        }

        benchmark(() -> {
            /* Split the task into threads. */
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new SumThread(numbers, i, i + (numberCount / threadCount));
                threads[i].start();
            }
            for (int i = 0; i < threadCount; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long sum = 0;
            for (int i = 0; i < threadCount; i++) {
                sum += threads[i].getSum();
            }
            System.out.println("Multi-threaded sum: " + sum);
        });
    }
}

class SumThread extends Thread {

    private int[] numbers;

    private int start;

    private int end;

    private long sum = 0;

    SumThread(int[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    public void run() {
        final long startTime = System.currentTimeMillis();
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        final long endTime = System.currentTimeMillis();
        final long elapsedTime = endTime - startTime;
        System.out.println(
            "Thread: " + Thread.currentThread().getName() + ", " +
            "Time taken: " + elapsedTime + "ms"
        );
    }

    long getSum() {
        return sum;
    }
}
