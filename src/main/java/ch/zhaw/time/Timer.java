package ch.zhaw.time;

import java.util.concurrent.TimeUnit;

public class Timer {

    public static String getFormattedTime(String format, long time){
        long millis = time;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        long milliseconds = millis - TimeUnit.MINUTES.toMillis(minutes) - TimeUnit.SECONDS.toMillis(seconds);
        return String.format(format, minutes,
                seconds, milliseconds);
    }
}
