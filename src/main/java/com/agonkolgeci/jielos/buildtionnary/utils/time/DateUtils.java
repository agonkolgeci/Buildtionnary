package com.agonkolgeci.jielos.buildtionnary.utils.time;

public class DateUtils {

    public static String formatSeconds(int value) {
        final int seconds = value % 60;
        final int minutes = value / 60;

        final StringBuilder sb = new StringBuilder();

        if(minutes > 0) {
            sb.append(minutes).append(" minute").append(minutes == 1 ? "" : "s");
        }

        if(seconds > 0) {
            if(minutes > 0) sb.append(", ");
            sb.append(seconds).append(" seconde").append(seconds == 1 ? "" : "s");
        }

        if(sb.isEmpty()) {
            return "0 seconde";
        }

        return sb.toString();
    }

}
