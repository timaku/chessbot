package util;

public class Timer {
    private long startTime;
    private long endTime;
    private long elapsed;
    
    public Timer() {
        startTime = -1;
        endTime = -1;
        elapsed = 0;
    }

    public void start() {
        startTime = System.nanoTime();
    }
    
    public void stop() {
        endTime = System.nanoTime();
        elapsed = endTime - startTime;
    }
    
    public long getDuration() {
        return elapsed;
    }
    
    public long getStart() {
        return startTime;
    }
    
    public long getStop() {
        return endTime;
    }

}