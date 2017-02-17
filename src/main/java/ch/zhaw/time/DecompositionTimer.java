package ch.zhaw.time;

public class DecompositionTimer {
    private static boolean calculationFlag = true;
    private static long startTime;
    private static long endTime;

    public static void start(){
        if (calculationFlag){
            startTime = System.currentTimeMillis();
        }
    }
    public static void stop(){
        if (calculationFlag){
            endTime = System.currentTimeMillis();
            calculationFlag = false;
        }
    }
    public static long getTime(){
        return endTime - startTime;
    }

    public static String getFormattedTime(){
        return Timer.getFormattedTime("%02d min, %02d.%03d sec", getTime());
    }
}
