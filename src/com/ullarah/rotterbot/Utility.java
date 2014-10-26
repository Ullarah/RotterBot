package com.ullarah.rotterbot;

import java.util.Random;

public class Utility {

    public static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static String stringJoin(String[] w, String c) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : w) stringBuilder.append(s).append(c);
        return stringBuilder.toString();
    }

    public static String timeConversion(int s) {
        final int minutesHour = 60;
        final int secondsMinute = 60;

        s = s % secondsMinute;

        int m = (s / secondsMinute) % minutesHour;
        int h = (s / secondsMinute) / minutesHour;

        return h + ":" + m + ":" + s;
    }

    public static void setLastMessage(String u, String m) {
        Client.recallMessages.put(u, m);
    }

    public static String getLastMessage(String u) {
        return Client.recallMessages.get(u);
    }

}