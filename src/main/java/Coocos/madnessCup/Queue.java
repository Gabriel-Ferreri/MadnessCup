package Coocos.madnessCup;

public class Queue {
    private int max_capacity;
    private int min_capacity;

    public Queue() {
        this.max_capacity = 8;
        this.min_capacity = 2;
    }

    public int getMin_capacity() {
        return min_capacity;
    }
    public int getMax_capacity() {
        return max_capacity;
    }
    public void setMin_capacity(int min_capacity) {
        if (min_capacity <= this.max_capacity && min_capacity > 1)
            this.min_capacity = min_capacity;
    }

    public void setMax_capacity(int max_capacity) {
        if (max_capacity > 1 && max_capacity > min_capacity)
            this.max_capacity = max_capacity;
    }
}
