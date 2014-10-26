package com.ullarah.rotterbot.modules;

import com.ullarah.rotterbot.Utility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Youtube {

    private static final String getRegex = "(?:youtube.+?(?:v=|/v/|embed/)|youtu\\.be/|yooouuutuuube\\.com.+yt=)([-_a-zA-Z0-9]{11})";
    private static final Pattern videoPattern = Pattern.compile(getRegex);

    public static Boolean isVideo(String s) {

        return videoPattern.matcher(s).find();

    }

    public static String getVideoID(String s) {

        Matcher m = videoPattern.matcher(s);

        if (m.find()) return m.group(1);

        return null;

    }

    public static String getVideoInfo(String s) {

        try {
            s = s.replaceAll(" ", "+");

            URL url = new URL("http://gdata.youtube.com/feeds/api/videos/" + s + "?v=2&alt=json");
            URLConnection conn = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            JSONObject entryData = (JSONObject) jsonObject.get("entry");

            JSONObject entryTitle = (JSONObject) entryData.get("title");
            String videoTitle = (String) entryTitle.get("$t");

            JSONArray authorArray = (JSONArray) entryData.get("author");
            JSONObject authorObject = (JSONObject) authorArray.get(0);
            JSONObject authorName = (JSONObject) authorObject.get("name");
            String videoAuthor = (String) authorName.get("$t");

            JSONObject viewObject = (JSONObject) entryData.get("yt$statistics");
            String videoViews = (String) viewObject.get("viewCount");

            JSONObject mediaObject = (JSONObject) entryData.get("media$group");
            JSONObject durationObject = (JSONObject) mediaObject.get("yt$duration");
            String videoDuration = (String) durationObject.get("seconds");

            JSONArray categoryArray = (JSONArray) entryData.get("category");
            JSONObject categoryObject = (JSONObject) categoryArray.get(1);
            String videoCategory = (String) categoryObject.get("term");

            JSONObject ratingObject = (JSONObject) entryData.get("yt$rating");
            String videoLikes = (String) ratingObject.get("numLikes");
            String videoDislikes = (String) ratingObject.get("numDislikes");

            return "[YOUTUBE] " + Colour.BOLD + videoTitle + Colour.RESET +
                    " | " + videoAuthor +
                    " | " + videoCategory +
                    " | " + videoViews +
                    " | " + Colour.GREEN + videoLikes + Colour.RESET +
                    " / " + Colour.RED + videoDislikes + Colour.RESET +
                    " | " + Utility.timeConversion(Integer.parseInt(videoDuration));

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;

    }

}
