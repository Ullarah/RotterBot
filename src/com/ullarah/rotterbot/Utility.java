package com.ullarah.rotterbot;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Random;

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
        Client.recallMessages.put(u, m);
    }

    public static String getLastMessage(String u) {
        return Client.recallMessages.get(u);
    }

    public static String urlEncode(String u) throws UnsupportedEncodingException {
        return URLEncoder.encode(u, "UTF-8");
    }

    public static String urlDecode(String u) throws UnsupportedEncodingException {
        return URLDecoder.decode(u, "UTF-8");
    }

}
