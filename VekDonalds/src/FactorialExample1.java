public class FactorialExample1 {

    private static final int repetitions = 100_000;

    private static final int threadCount = 4;
    private static int k = 0;

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

    private static void fact(int n) {
        @SuppressWarnings("unused")
        long result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
    }

    public static void main(String[] args) {
        // 0, 4, 8, 12
        // 1, 5, 9, 13
        // 2, 6, 10, 14
        // 3, 7, 11, 15
        final Runnable factorial = new Runnable() {
            @Override
            public void run() {
                int count = 1;
                for (int i = k; i < repetitions; i+=threadCount) {
                    fact(i);
                    count++;
                }
                System.out.println(
                    "Thread completed " +
                    count +
                    " factorial computations."
                );
            }
        };
        
        final Thread[] threads = new Thread[threadCount];
        benchmark(() -> {
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(factorial);
                threads[i].start();
                k++;
            }
            for (int i = 0; i < threadCount; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
