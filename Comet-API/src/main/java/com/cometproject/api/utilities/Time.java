package com.cometproject.api.utilities;

import java.util.concurrent.TimeUnit;

public class Time {

    public static long getMinutesMillis(int value) {
        return TimeUnit.MINUTES.convert(value, TimeUnit.MILLISECONDS);
    }

    public static long getDayMillis(int value) {
        return TimeUnit.DAYS.convert(1, TimeUnit.MILLISECONDS);
    }

}