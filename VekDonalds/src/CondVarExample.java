import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class CondVarExample {

    private static final Lock mutex = new ReentrantLock();

    private static final Condition condNumDone = mutex.newCondition();

    private static final Condition condCharDone = mutex.newCondition();

    private static volatile char buffer[] = new char[20];

    private static volatile int index = 0;

    private static void randomSleep() {
        try {
            final int n = (int)(Math.random() * 10);
            Thread.sleep(n);
        } catch (InterruptedException e) {
        }
    }

    private static void mutexLock(Consumer<Integer> action, int i) {
        mutex.lock();
        try {
            action.accept(i);
        } finally {
            mutex.unlock();
        }
    }

    public static void main(String[] args) {
        final Thread numThread = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				// buffer[index++] = (char)('0' + i);
				// randomSleep();
				mutexLock((ii) -> {
					while (index % 2 == 1) {
						/* Release the lock and wait until another thread
						   calls condCharDone.signal */
						try {
							condCharDone.await();
						} catch (InterruptedException e) {
						}
					}
					buffer[index] = (char)('0' + ii);
					index++;
					randomSleep();
					condNumDone.signal();
				}, i);
			}
		});
        final Thread charThread = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				// buffer[index++] = (char)('A' + i);
				// randomSleep();
				mutexLock((ii) -> {
					while (index % 2 == 0) {
						/* Release the lock and wait until another thread
						   calls condNumDone.signal */
						try {
							condNumDone.await();
						} catch (InterruptedException e) {
						}
					}
					buffer[index] = (char)('A' + ii);
					index++;
					randomSleep();
					condCharDone.signal();
				}, i);
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
