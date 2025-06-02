public class ThreadExample {

    public static void main(String[] args) {
        final String threadName = Thread.currentThread().getName();
        System.out.println(threadName + " is now running");

        final MyThread thread1 = new MyThread();
        final MyThread thread2 = new MyThread();
        
        thread1.start();
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
        System.out.println("Done!");
    }
}

class MyThread extends Thread {

    @Override
    public void run() {
        final String threadName = Thread.currentThread().getName();
        System.out.println(threadName + " is now running");
    }
}
