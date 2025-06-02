public class Exercise {

    public static void f(int n){
        long result = 1;
        for(int i = 1 ; i <= n ; i++){
            result *= i;
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        for(int i = 0 ; i < 100_000 ; ++i){
            f(i);
        }
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        System.out.println("Done!" + elapsed + "ms.");
    }
}