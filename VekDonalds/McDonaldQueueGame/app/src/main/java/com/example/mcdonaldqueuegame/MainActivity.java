package com.example.mcdonaldqueuegame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import android.graphics.Bitmap;
import com.example.mcdonaldqueuegame.GameOverException;
import android.graphics.PorterDuff;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.BitmapFactory;

public class MainActivity extends AppCompatActivity {

    private final int HOTDOG_POOL_SIZE = 10;
    private final int CHEF_LIMIT = 10;
    private final int CASHIER_LIMIT = 10;

    private ArrayList<Customer> customers = new ArrayList<>();
    private ArrayList<Cashier> cashiers = new ArrayList<>(); // Replace cashierCount with list
    private ArrayList<Chef> chefs = new ArrayList<>();
    private ArrayList<Thread> chefThreadPool = new ArrayList<>();
    private ArrayList<Thread> cashierThreadPool = new ArrayList<>();
    private int preparedHotdogs = 0;
    public int cash = 0;
    private TextView txtCashierCount, txtCustomerQueue, txtCash, txtChefCount, txtHotdogCount;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Thread gameLoopThread;
    private boolean isGameLoopRunning = false;
    final HotdogPool hotdogPool = new HotdogPool(HOTDOG_POOL_SIZE);

    private float customerSpacing = 100; // Space between customers
    private int counterWidth = 400; // Width of the counter
    private int counterHeight = 100; // Height of the counter
    private final float FRAME_TIME = 0.030f; // 30ms per frame

    private Bitmap customerBitmap;
    private Bitmap chefBitmap;
    private Bitmap scaledBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtCashierCount = findViewById(R.id.txtCashierCount);
        txtHotdogCount = findViewById(R.id.txtHotdogCount);
        txtCustomerQueue = findViewById(R.id.txtCustomerQueue);
        txtChefCount = findViewById(R.id.txtChefCount);
        txtCash = findViewById(R.id.txtCash);

        updateChefCount();

        surfaceView = findViewById(R.id.surfaceView);

        surfaceHolder = surfaceView.getHolder();
        //sprite rendering
        surfaceView.setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        ImageButton backButton = findViewById(R.id.backButton);

        Button btnIncreaseCashier = findViewById(R.id.btnIncreaseCashier);
        Button btnDecreaseCashier = findViewById(R.id.btnDecreaseCashier);

        Button btnIncreaseChef = findViewById(R.id.btnIncreaseChef);
        Button btnDecreaseChef = findViewById(R.id.btnDecreaseChef);


        backButton.setOnClickListener(v -> {
            System.out.println("test");
            finish();
        });

        btnIncreaseCashier.setOnClickListener(v -> {

            if(cashiers.size() < CASHIER_LIMIT) {
                btnDecreaseCashier.setEnabled(true);
                addCashier();
            }else{
                btnIncreaseCashier.setEnabled(false);
            }
        });

        btnDecreaseCashier.setOnClickListener(v -> {
            if(cashiers.size() > 0) {
                btnIncreaseCashier.setEnabled(true);
                removeCashier();
            }else{
                btnDecreaseCashier.setEnabled(false);
            }
        });

        btnIncreaseChef.setOnClickListener(v -> {
            if(chefs.size() < CHEF_LIMIT) {
                btnDecreaseChef.setEnabled(true);
                addChef();
            }else{
                btnIncreaseChef.setEnabled(false);
            }
        });

        btnDecreaseChef.setOnClickListener(v -> {
            if(chefs.size() > 0) {
                btnIncreaseChef.setEnabled(true);
                removeChef();
            }else{
                btnDecreaseChef.setEnabled(false);
            }
        });

        // Start with 1 cashier
        addCashier();

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                startGameLoop();
                customerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.customer);
                customerBitmap = Bitmap.createScaledBitmap(customerBitmap, 160, 160,true);
                chefBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.chef);
                chefBitmap = Bitmap.createScaledBitmap(chefBitmap, 160, 160,true);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                updateCashierPositions();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                stopGameLoop();
            }
        });
    }
    private void initializeChefs(int numChefs) {
        float chefSpacing = surfaceView.getWidth() / (numChefs + 1);
        for (int i = 0; i < numChefs; i++) {
            float chefX = (surfaceView.getWidth() - counterWidth) / 2 + chefSpacing * (i + 1);
            float chefY = surfaceView.getHeight() / 4; // Top quarter of the screen
            chefs.add(new Chef(chefX, chefY, hotdogPool));
        }
    }

    // Add this method to update the hotdog pool display:
    private void updateHotdogPoolDisplay() {
        runOnUiThread(() -> {
            txtHotdogCount.setText("Hotdogs: " + hotdogPool.getHotdogCount());
        });
    }


    // Add a cashier to the counter
    private void addCashier() {
        synchronized (hotdogPool) {
            Cashier cashier = new Cashier(0, 0, hotdogPool); // Position will be updated
            cashiers.add(cashier); // Position will be updated
            Thread cashierThread = new Thread(cashier);
            cashierThreadPool.add(cashierThread);
            cashierThread.start();
        }
        updateCashierPositions();
        updateCashierCount();
    }

    // Remove a cashier from the counter
    private void removeCashier() {
        if (!cashiers.isEmpty()) {
            Cashier cashier = cashiers.remove(cashiers.size() - 1);
            Thread cashierThread = cashierThreadPool.remove(cashierThreadPool.size() - 1);

            cashier.terminate();
            cashierThread.interrupt();

            // If cashier was serving, put customer back in queue
            if (cashier.isServing() && cashier.getCurrentCustomer() != null) {
                Customer customer = cashier.getCurrentCustomer();
                customers.add(0, customer); // Add back to front of queue
            }

            try {
                cashierThread.join(50); // Increased timeout
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Log.d("CASHIER_THREAD", "Cashier state: " + cashierThread.isAlive());    

            updateCashierPositions();
            updateCashierCount();
        }
    }

    // Modified addChef() with proper thread tracking
    private void addChef() {
        float chefSpacing = surfaceView.getWidth() / (chefs.size() + 2);
        float chefX = (surfaceView.getWidth() - counterWidth)/2 + chefSpacing*(chefs.size()+1);
        float chefY = surfaceView.getHeight()/4;
        
        synchronized(hotdogPool) {
            Chef chef = new Chef(chefX, chefY, hotdogPool);
            Thread chefThread = new Thread(chef);
            chefs.add(chef);
            chefThreadPool.add(chefThread); // Track threads
            chefThread.start();
        }
        updateChefsOnCanvas();
    }

    private void removeChef() {
        if(!chefs.isEmpty()) {
            Chef chef = chefs.remove(chefs.size()-1);
            Thread chefThread = chefThreadPool.remove(chefThreadPool.size()-1);
            
            chef.finishCooking();
            chefThread.interrupt();
            
            try {
                chefThread.join(50); // Reduce join timeout
                if(chefThread.isAlive()) {
                    Log.e("THREAD", "Failed to terminate chef!");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Log.d("THREAD", "Chef state: " + chefThread.isAlive());
            updateChefsOnCanvas();
            updateChefCount();
        }
    }    

    private void updateChefsOnCanvas() {
        int numChefs = chefs.size();
        if (numChefs == 0) return;

        int screenWidth = surfaceView.getWidth();
        int chefAreaY = surfaceView.getHeight() / 4; // Top quarter of the screen
        float spacing = screenWidth / (numChefs + 1);

        for (int i = 0; i < numChefs; i++) {
            Chef chef = chefs.get(i);
            chef.setX(spacing * (i + 1));
            chef.setY(chefAreaY);
        }
    }
//    private void updateChefCountDisplay() {
//        TextView txtChefCount = findViewById(R.id.txtChefCount);
//        txtChefCount.setText("Chefs: " + chefs.size());
//    }
    private void updateCashierCount() {
        txtCashierCount.setText("Cashiers: " + cashiers.size());
    }

    private void updateChefCount() {
        txtChefCount.setText("Chef: " + chefs.size());
    }

    private void updateCashCount() {
        runOnUiThread(() -> {
            txtCash.setText("Cash: " + cash);
        });
    }

    // Position cashiers evenly along the counter
    private void updateCashierPositions() {
        int numCashiers = cashiers.size();
        if (numCashiers == 0) return;

        int screenWidth = surfaceView.getWidth();
        int counterY = surfaceView.getHeight() / 2;
        int counterX = (screenWidth - counterWidth) / 2;

        float spacing = counterWidth / (numCashiers + 1);
        float startX = counterX + spacing;

        for (int i = 0; i < numCashiers; i++) {
            Cashier cashier = cashiers.get(i);
            cashier.setX(startX + i * spacing);
            cashier.setY(counterY);
        }
    }

    private void startGameLoop() {
        if (isGameLoopRunning) return;

        isGameLoopRunning = true;

        gameLoopThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    // Add new customer periodically
                    surfaceView.post(() -> {
                        if (Math.random() < 0.02) { // Adjust spawn rate as needed
                            addCustomer();
                        }
                    });
                    //update hotdog
                    updateHotdogPoolDisplay();
                    // update cash
                    updateCashCount();

                    // Move customers to their positions in the queue
                    updateCustomerPositions();

                    // Process cashiers and customer service
                    try {processCustomerService();}
                    catch (GameOverException e) {
                        return;
                    }

                    // Update UI and redraw canvas
                    runOnUiThread(() -> {
                        txtCustomerQueue.setText("Queue: " + customers.size() + " customers");
                        drawCanvas();
                    });

                    Thread.sleep(30); // Control game loop speed
                }
            } catch (InterruptedException e) {
                Log.e("GameLoop", "Game loop interrupted", e);
            }
        });

        gameLoopThread.start();
    }
    private void processChefCooking() {
        for (Chef chef : chefs) {
            if (!chef.isCooking() && preparedHotdogs < customers.size()) {
                chef.startCooking();
            }
            if (chef.update(FRAME_TIME)) { // If cooking is finished
                preparedHotdogs++;
            }
        }
    }
    // Handle customer service by cashiers
    // private void processCustomerService() throws GameOverException {
    //     // Update all cashiers and check for completed service
    //     for (Cashier cashier : cashiers) {
    //         cashier.update(FRAME_TIME); // Update service timer
    //         if (cashier.isAvailable() && !customers.isEmpty() && preparedHotdogs > 0) {
    //             Customer firstCustomer = customers.remove(0);
    //             cashier.startServing(firstCustomer);
    //             preparedHotdogs--;
    //         }
    //     }

    //     // Assign waiting customers to available cashiers
    //     for (Cashier cashier : cashiers) {
    //         if (cashier.isAvailable() && !customers.isEmpty()) {
    //             Customer firstCustomer = customers.get(0);

    //             // Only assign if customer has reached the front of the queue
    //             if (firstCustomer.getY() <= surfaceView.getHeight() / 2 + counterHeight + customerSpacing) {
    //                 if (hotdogPool.isEmpty()) {
    //                     System.out.println("Game over, Not able to make enough hotdogs on time for customers");
    //                     throw new GameOverException("Not able to make enough hotdogs on time for customers");}
    //                 customers.remove(0);
    //                 cashier.startServing(firstCustomer);
    //                 cash += 10;

    //                 // Position customer in front of their cashier
    //                 firstCustomer.setX(cashier.getX());
    //                 firstCustomer.setY(cashier.getY() + 50); // Position in front of cashier
    //             }
    //         }
    //     }
    // }

    private void processCustomerService() throws GameOverException {
        for(Cashier cashier : cashiers) {
            cashier.update(FRAME_TIME);
            if(cashier.isAvailable() && !customers.isEmpty()) {
                Customer firstCustomer = customers.get(0);
                if(firstCustomer.getY() <= surfaceView.getHeight()/2 + counterHeight + customerSpacing) {
                    if (firstCustomer.getY() <= surfaceView.getHeight() / 2 + counterHeight + customerSpacing) {
                        synchronized (hotdogPool) {
                            if (hotdogPool.isEmpty()) {
                                System.out.println("Game over, Not able to make enough hotdogs on time for customers");
                                throw new GameOverException("Not able to make enough hotdogs on time for customers");}
                        customers.remove(0);
                        cashier.startServing(firstCustomer);
                        cash += 10;
                        firstCustomer.setX(cashier.getX());
                        firstCustomer.setY(cashier.getY() + 50);
                        hotdogPool.get();
                        }
                    }
                }
            }
        }
    }

    private void updateCustomerPositions() {
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);

            // Calculate target position: starts below the counter at increasing distances
            float targetY = surfaceView.getHeight() / 2 + counterHeight + (i + 1) * customerSpacing;

            // Move customer toward target position
            if (customer.getY() > targetY) {
                customer.move(); // Move upward
            }

            // Stop at target position
            if (customer.getY() <= targetY) {
                customer.setY(targetY);
            }
        }
    }

    private void stopGameLoop() {
        if (gameLoopThread != null && gameLoopThread.isAlive()) {
            gameLoopThread.interrupt();
            try {
                gameLoopThread.join();
            } catch (InterruptedException e) {
                Log.e("GameLoop", "Error stopping game loop thread", e);
            }
        }
        isGameLoopRunning = false;
    }

    private void addCustomer() {
        if (customers.isEmpty()) {
            // First customer starts at the bottom of the screen
            customers.add(new Customer(surfaceView.getWidth() / 2, surfaceView.getHeight(), 5));
        } else {
            // Add new customer below the last one in the queue
            Customer lastCustomer = customers.get(customers.size() - 1);
            float newY = lastCustomer.getY() + customerSpacing;
            customers.add(new Customer(surfaceView.getWidth() / 2, surfaceView.getHeight(), 5));
        }
    }

    private void drawCanvas() {
        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas != null) {
            try {
//                canvas.drawColor(0xFFFFFFFF); // Clear screen with white color

                //sprite rendering
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // ✅ Transparent clear

                Paint paint = new Paint();

                // Draw the counter at the center of the screen
                paint.setColor(0xFF8B4513); // Brown color for counter
                int screenWidth = surfaceView.getWidth();
                int screenHeight = surfaceView.getHeight();
                int counterX = (screenWidth - counterWidth) / 2; // Center horizontally
                int counterY = screenHeight / 2; // Center vertically
                canvas.drawRect(counterX, counterY, counterX + counterWidth, counterY + counterHeight, paint);

                // Draw cashiers (from top-down view)
                drawCashiers(canvas, paint);
                drawChefs(canvas, paint);

                // Draw customers in the queue
                paint.setColor(0xFF000000); // Black color for customers
                for (Customer customer : customers) {
                    //sprite rendering
                    canvas.drawBitmap(customerBitmap, customer.getX() - customerBitmap.getWidth() / 2f,
                            customer.getY() - customerBitmap.getHeight() / 2f, null);
//                    canvas.drawCircle(customer.getX(), customer.getY(), 40, paint);
                }
                for (Chef chef : chefs) {
                    //sprite rendering
                    canvas.drawBitmap(chefBitmap, chef.getX() - chefBitmap.getWidth() / 2f,
                            chef.getY() - chefBitmap.getHeight() / 2f, null);
//                    canvas.drawCircle(chef.getX(), chef.getY(), 40, paint);
                }
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
    private void drawChefs(Canvas canvas, Paint paint) {
        for (Chef chef : chefs) {
            paint.setColor(0xFFFF0000); // White for chef's uniform
            canvas.drawCircle(chef.getX(), chef.getY(), 30, paint); // Circle representing the chef

            // Draw chef's hat
            paint.setColor(0xFFCCCCCC); // Light gray for hat
            canvas.drawRect(chef.getX() - 20, chef.getY() - 40, chef.getX() + 20, chef.getY() - 20, paint);

            // Indicate cooking status
            if (chef.isCooking()) {
                paint.setColor(0xFFFF0000); // Red for cooking status
                canvas.drawText("Cooking", chef.getX() - 30, chef.getY() - 50, paint);
            }
        }
    }

    private void drawCounter(Canvas canvas, Paint paint) {
        paint.setColor(0xFF8B4513); // Brown color for the counter
        int screenWidth = surfaceView.getWidth();
        int screenHeight = surfaceView.getHeight();
        int counterX = (screenWidth - counterWidth) / 2; // Center horizontally
        int counterY = screenHeight / 2; // Center vertically

        // Draw the counter rectangle
        canvas.drawRect(counterX, counterY, counterX + counterWidth, counterY + counterHeight, paint);
    }

    // Draw cashiers with top-down view
    private void drawCashiers(Canvas canvas, Paint paint) {
        for (Cashier cashier : cashiers) {
            // Draw cashier body (circle with arms)
            paint.setColor(0xFF000000); // Dark color for cashier

            // Draw circle for head/body
            canvas.drawCircle(cashier.getX(), cashier.getY(), 20, paint);

            // Draw arms
            paint.setStrokeWidth(8);

            // Left arm
            canvas.drawLine(
                    cashier.getX() - 10, cashier.getY() - 5,
                    cashier.getX() - 25, cashier.getY() - 15,
                    paint
            );

            // Right arm
            canvas.drawLine(
                    cashier.getX() + 10, cashier.getY() - 5,
                    cashier.getX() + 25, cashier.getY() - 15,
                    paint
            );

            // Draw legs
            canvas.drawLine(
                    cashier.getX() - 5, cashier.getY() + 10,
                    cashier.getX() - 15, cashier.getY() + 25,
                    paint
            );

            canvas.drawLine(
                    cashier.getX() + 5, cashier.getY() + 10,
                    cashier.getX() + 15, cashier.getY() + 25,
                    paint
            );

            // Draw status text
            paint.setTextSize(18);
            if (cashier.isServing()) {
                // Draw service progress bar
                paint.setColor(0xFF00FF00); // Green for progress
                float progress = cashier.getServiceProgress();
                canvas.drawRect(
                        cashier.getX() - 25,
                        cashier.getY() - 40,
                        cashier.getX() - 25 + 50 * progress,
                        cashier.getY() - 35,
                        paint
                );

                // Draw status
                paint.setColor(0xFF000000);
                canvas.drawText("Serving", cashier.getX() - 30, cashier.getY() - 45, paint);
            } else {
                paint.setColor(0xFF000000);
                canvas.drawText("Idle", cashier.getX() - 15, cashier.getY() - 40, paint);
            }

            // Draw customers being served
            if (cashier.isServing() && cashier.getCurrentCustomer() != null) {
                Customer customer = cashier.getCurrentCustomer();
                paint.setColor(0xFF000000); // Black for customer
                canvas.drawCircle(customer.getX(), customer.getY(), 40, paint);
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopGameLoop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameLoopRunning) startGameLoop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGameLoop();
    }
}
    