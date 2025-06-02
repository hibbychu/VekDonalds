public class VolatileExample {

    private static volatile boolean done = false;
    // private static boolean done = false;

    private static void sleep(int n) {
        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) {
        final Thread thread1 = new Thread(() -> {
			while (!done);
			System.out.println("Done!");
		});
		
        final Thread thread2 = new Thread(() -> {
			done = true;
		});

        thread1.start();
        sleep(100);
        thread2.start();

        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
