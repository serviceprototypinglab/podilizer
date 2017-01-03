package ch.zhaw.file_operations;

public class Measurement {
    private long prevt;

    public Measurement() {
        prevt = System.currentTimeMillis();;
    }

    public void measure(String tag) {
        long t = System.currentTimeMillis();
        long delta = t - prevt;
        System.out.println("MEASURE [" + new Long(delta).toString() + "] " + tag);
        prevt = t;
    }
}
