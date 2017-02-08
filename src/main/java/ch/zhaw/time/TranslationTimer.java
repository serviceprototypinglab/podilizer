package ch.zhaw.time;

public class TranslationTimer {
    private static long startTime;
    private static long endTime;

    public static void start(){
        startTime = System.currentTimeMillis();
    }
    public static void stop(){
        endTime = System.currentTimeMillis();
    }
    public static long getTime(){
        return endTime - startTime;
    }

    public static String getFormattedTime(){
        return Timer.getFormattedTime("%02d min, %02d.%03d sec", getTime());
    }
}
