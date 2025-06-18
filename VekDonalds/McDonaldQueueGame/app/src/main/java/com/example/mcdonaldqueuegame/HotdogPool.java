package com.example.mcdonaldqueuegame;

import android.util.Log;

class HotdogPool {

    private volatile Hotdog[] buffer;

    private volatile int front = 0;

    private volatile int back = 0;

    private volatile int itemCount = 0;

    HotdogPool(int size) {
        buffer = new Hotdog[size];
    }

    synchronized boolean isEmpty() {return itemCount == 0;}

    synchronized void put(Hotdog hotdog) {
        while (itemCount == buffer.length) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
        buffer[back] = hotdog;
        back = (back + 1) % buffer.length;
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
        Hotdog hotdog = buffer[front];
        front = (front + 1) % buffer.length;
        itemCount--;
        Log.d("POOL", "Served hotdog. Remaining: " + itemCount);
        this.notifyAll();
        return hotdog;
    }

    synchronized int getHotdogCount(){
        return itemCount;
    }
}