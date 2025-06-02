public class LambdaExample {

    public static void main(String[] args) {
        final Thread thread1 = new Thread(() -> {
            System.out.println("first thread is running");
        });
        final Thread thread2 = new Thread(() -> {
            System.out.println("second thread is running");
        });
        
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