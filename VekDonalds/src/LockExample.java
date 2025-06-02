public class LockExample {

    private static final long maxElement = 100_000;

    private static final int threadCount = 2;

    private static final Object lock = new Object();

    private static long sum = 0;

    public static void main(String[] args) {
        final Runnable adder = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < maxElement; i++) {
                    // sum++;
                    synchronized(lock) {
                        sum++;
                    }
                }
            }
        };
        final Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(adder);
            threads[i].start();
        }
        for (int i = 0; i < threadCount; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("sum = " + sum);
    }
}
