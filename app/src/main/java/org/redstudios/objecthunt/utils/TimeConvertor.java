package org.redstudios.objecthunt.utils;

public class TimeConvertor {

    public String getTimeFromSeconds(long sec) {
        String time;
        int seconds = (int) sec;
        int hours;
        int minutes;

        hours = seconds / 3600;
        seconds = seconds % 3600;
        minutes = seconds / 60;
        seconds = seconds % 60;

        time = hours + ":" + minutes + ":" + seconds;
        return time;
    }
}
