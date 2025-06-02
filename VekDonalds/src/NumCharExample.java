import java.util.function.Consumer;

public class NumCharExample {

    private static volatile char buffer[] = new char[20];

    private static volatile int index = 0;

	private static final Object lock = new Object();

	private static void lockThis(Consumer<Integer> action, int i) {
		synchronized(lock) {
			action.accept(i);
		}
	}

	private static void randomSleep() {
        try {
            final int n = (int)(Math.random() * 10);
            Thread.sleep(n);
        } catch (InterruptedException e) {
        }
    }

    public static void main(String[] args) {
        final Thread numThread = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				lockThis((ii) -> {
					buffer[index++] = (char)('0' + ii);
				},  i);
				randomSleep();
			}
		});
		final Thread charThread = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				lockThis((ii) -> {
					buffer[index++] = (char)('A' + ii);
				}, i);	
				randomSleep();
			}
		});
		
        final Thread[] threads = {
			numThread,
			charThread	
		};
		
		for (Thread thread : threads) {
			thread.start();
		}

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(buffer);
    }
}
