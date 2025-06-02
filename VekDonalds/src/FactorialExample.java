public class FactorialExample {

    private static final int repetitions = 100_000;

    private static final int threadCount = 1;

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
        final Runnable factorial = new Runnable() {
            @Override
            public void run() {
                int count = 1;
                for (int i = 1; i < repetitions; i++) {
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
