package com.example.mcdonaldqueuegame;

public class Chef implements Runnable {
    private float x, y;
    private boolean isCooking;
    private float cookingTimer;
    private static final float COOKING_TIME = 10.0f; // 3 seconds to prepare a hotdog

    private volatile HotdogPool hotDogPool;

    public Chef(float x, float y, HotdogPool hotDogPool) {
        this.x = x;
        this.y = y;
        this.isCooking = true;
        this.cookingTimer = 0;
        this.hotDogPool = hotDogPool;
    }

    public void startCooking() {
        this.isCooking = true;
        this.cookingTimer = 0;
    }

    public boolean update(float deltaTime) {
        if (!isCooking)
            return false;

        cookingTimer += deltaTime;
        if (cookingTimer >= COOKING_TIME) {
            finishCooking();
            return true;
        }
        return false;
    }

    public void finishCooking() {
        isCooking = false;
        cookingTimer = 0;
    }

    static void makeHotDog() { // Unit of time to make hotdog
        try {
            Thread.sleep(4000); // Sleep for 4 seconds
        } catch (InterruptedException e) {
            // Handle the exception
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && isCooking) {
            try {
                makeHotDog();
                if (Thread.interrupted()) throw new InterruptedException();
                synchronized(hotDogPool) {
                    hotDogPool.put(new Hotdog());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Critical line
                break;
            }
        }
    }
    

    // Getters and setters
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public boolean isCooking() {
        return isCooking;
    }

    public float getCookingProgress() {
        return cookingTimer / COOKING_TIME;
    }
}