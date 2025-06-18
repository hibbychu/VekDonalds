package com.example.mcdonaldqueuegame;

public class Cashier implements Runnable {
    private float x, y; // Position coordinates
    private boolean isServing;
    private Customer currentCustomer;
    private float serviceTimer;
    private static final float SERVICE_TIME = 5.0f; // 5 seconds service time
    private volatile boolean shouldTerminate = false;

    private static volatile HotdogPool hotDogPool; // Shared resource

    public Cashier(float x, float y, HotdogPool hotDogPool) {
        this.x = x;
        this.y = y;
        this.isServing = false;
        this.currentCustomer = null;
        this.serviceTimer = 0;

        Cashier.hotDogPool = hotDogPool; // Initialize the shared resource
    }

    public void terminate() {
        shouldTerminate = true;
    }

    public boolean isAvailable() {
        return !isServing;
    }

    public void startServing(Customer customer) {
        this.isServing = true;
        this.currentCustomer = customer;
        this.serviceTimer = 0;
    }

    // Update method - returns true if service just completed
    public boolean update(float deltaTime) {
        if (!isServing) return false;

        serviceTimer += deltaTime;

        if (serviceTimer >= SERVICE_TIME) {
            finishServing();
            return true;
        }

        return false;
    }

    public Customer finishServing() {
        Customer served = currentCustomer;
        isServing = false;
        currentCustomer = null;
        serviceTimer = 0;
        return served;
    }

    // Getters and setters
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public boolean isServing() { return isServing; }
    public Customer getCurrentCustomer() { return currentCustomer; }
    public float getServiceTimer() { return serviceTimer; }
    public float getServiceProgress() { return serviceTimer / SERVICE_TIME; }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && !shouldTerminate) {
            try {
                if (isServing()) {
                    update(0.03f); // Update service timer
                }
                Thread.sleep(30); // Simulate periodic updates
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
                break; // Exit loop on interruption
            }
        }
    }
}
