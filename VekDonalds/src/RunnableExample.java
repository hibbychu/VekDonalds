public class RunnableExample {

    public static void main(String[] args) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String thread_name = Thread.currentThread().getName();
                System.out.println(thread_name + " is running");
            }
        };

        final Thread thread1 = new Thread(runnable, "first thread");
        final Thread thread2 = new Thread(runnable, "second thread");

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
