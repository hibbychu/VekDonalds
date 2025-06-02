public class ProducerConsumer {

    private static final long limit = 300_000_000;

    private static int n;

    private static int size;

    private static void doWork(int n) {
        for (int i = 0; i < n; i++) {
            long m = limit;
            while (m > 0) {
                m--;
            }
        }
    }

    public static void main(String[] args) {
        n = Integer.parseInt(args[0]);
        size = Integer.parseInt(args[1]);
        final Buffer buffer = new Buffer(size);

		final Thread producer = new Thread(() -> {
			for (int i = 0; i < n; i++) {
				doWork(2);
				final Hotdog hotdog = new Hotdog(i);
				buffer.put(hotdog);
			}
		});
		
		final Thread consumer = new Thread(() -> {
			for (int i = 0; i < n; i++) {
				@SuppressWarnings("unused")
				final Hotdog hotdog = buffer.get();
				doWork(5);
			}
		});
		
        final Thread[] threads = {
			producer,
			consumer	
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
    }
}

class Buffer {

    private static volatile Hotdog[] buffer;

    private static volatile int front = 0;
    
    private static volatile int back = 0;
    
    private static volatile int itemCount = 0;

    Buffer(int size) {
        buffer = new Hotdog[size];
    }

    synchronized void put(Hotdog hotdog) {
        while (itemCount == buffer.length) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
        buffer[back] = hotdog;
        back = (back + 1) % buffer.length;
        System.out.println(
            "Item count: " + itemCount + ", " +
            "Producing " + hotdog
        );
        itemCount++;
        this.notifyAll();
    }

    synchronized Hotdog get() {
        while (itemCount == 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
        final Hotdog hotdog = buffer[front];
        front = (front + 1) % buffer.length;
        System.out.println(
            "Item count: " + itemCount + ", " +
            "Consuming " + hotdog
        );
        itemCount--;
        this.notifyAll();
        return hotdog;
    }
}

class Hotdog {
    
    private int id;

    public Hotdog(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Hotdog [id=" + id + "]";
    }
}
