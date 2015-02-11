package com.ullarah.rotterbot;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static com.ullarah.rotterbot.Messages.recallMessage;

public class Utility {

    public static int randInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static String stringJoin(String[] w, String c) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : w) stringBuilder.append(s).append(c);
        return stringBuilder.toString();
    }

    public static String timeConversion(int s) {
        long longVal = ((long) s);
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        if (mins == 0)
            return secs + " Seconds";

        if (hours == 0)
            return mins + ":" + secs;

        return hours + ":" + mins + ":" + secs;
    }

    public static void setLastMessage(String u, String m) {
        recallMessage.put(u, m);
    }

    public static String getLastMessage(String u) {
        return recallMessage.get(u);
    }

    public static String urlEncode(String u) throws UnsupportedEncodingException {
        return URLEncoder.encode(u, "UTF-8");
    }

    public static String urlDecode(String u) throws UnsupportedEncodingException {
        return URLDecoder.decode(u, "UTF-8");
    }

    private static String getCurrentTime() {
        Date cal = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return "[" + sdf.format(cal.getTime()) + "]";
    }

    public static void showLevelMessage(levelType type, String message) {

        System.out.println(getCurrentTime() + " " + type.text + " " + message);

    }

    public enum levelType {

        CHAT("[CHAT]"),
        INFO("[INFO]"),
        WARN("[WARN]"),
        ERROR("[ERROR]"),
        DEBUG("[DEBUG]");

        private final String text;

        private levelType(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

    }

}
