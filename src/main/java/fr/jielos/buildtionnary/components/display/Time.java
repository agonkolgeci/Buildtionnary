package fr.jielos.buildtionnary.components.display;

import org.bukkit.ChatColor;

public class Time {

    public static String formatSeconds(int value, ChatColor chatColor, boolean showSeconds) {
        int seconds = value % 60;
        int minutes = (value / 60) % 60;

        final String minutesDisplay = String.format("%d minute%s", minutes, (minutes > 1 ? "s" : ""));
        final String secondsDisplay = String.format("%d seconde%s", seconds, (seconds > 1 ? "s" : ""));

        if(minutes <= 0) {
            return chatColor + secondsDisplay;
        } else {
            if(!showSeconds || seconds <= 0) {
                return chatColor + minutesDisplay;
            }
        }

        return chatColor + minutesDisplay + ChatColor.GRAY + ", " + chatColor + secondsDisplay;
    }

}
