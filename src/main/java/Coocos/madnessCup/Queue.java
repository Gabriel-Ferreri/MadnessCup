package Coocos.madnessCup;

public class Queue {
    private int maxCapacity;
    private int minCapacity;
    private int currentCapacity;

    public Queue() {
        this.maxCapacity = 8;
        this.minCapacity = 2;
        this.currentCapacity = 0;
    }

    public int getMinCapacity() {
        return minCapacity;
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }
    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public void setMinCapacity(int minCapacity) {
        if (minCapacity <= this.maxCapacity && minCapacity > 1)
            this.minCapacity = minCapacity;
    }

    public void setMaxCapacity(int max_capacity) {
        if (maxCapacity > 1 && max_capacity > minCapacity)
            this.maxCapacity = max_capacity;
    }

    public void setCurrentCapacity(int current_capacity) {
        this.currentCapacity = current_capacity;
    }
}
