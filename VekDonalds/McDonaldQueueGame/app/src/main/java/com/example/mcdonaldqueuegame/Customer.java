package com.example.mcdonaldqueuegame;

public class Customer {

    private float x, y; // Position of the customer
    private int speed; // Speed of movement

    public Customer(float x, float y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    // Getters and setters for x and y coordinates
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

    // Method to move the customer upwards toward the center
    public void move() {
        if (y > 500) { // If the customer has not reached the center
            y -= speed; // Move the customer upwards
        }
    }
}
