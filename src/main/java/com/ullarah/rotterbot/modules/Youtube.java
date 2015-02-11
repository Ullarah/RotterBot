package com.ullarah.rotterbot.modules;

import com.ullarah.rotterbot.Utility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ullarah.rotterbot.Utility.urlDecode;
import static com.ullarah.rotterbot.Utility.urlEncode;

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

    public static String getVideoResult(String input) throws UnsupportedEncodingException {

        JSONObject entryData = getVideoEntry("http://gdata.youtube.com/feeds/api/videos?q=" +
                urlEncode(input) + "&alt=json&limit=1");

        JSONObject entryID = (JSONObject) entryData.get("id");

        String videoEntry = (String) entryID.get("$t");

        return getVideoInfo(videoEntry.substring(videoEntry.length() - 11));

    }

    private static JSONObject getVideoEntry(String url) {

        try {

            JSONParser jsonParser = new JSONParser();

            URLConnection conn = new URL(url).openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            reader.close();

            if (jsonObject.containsKey("entry")) {

                return (JSONObject) jsonObject.get("entry");

            } else if (jsonObject.containsKey("feed")) {

                JSONObject feedData = (JSONObject) jsonObject.get("feed");
                JSONArray feedEntry = (JSONArray) feedData.get("entry");

                return (JSONObject) feedEntry.get(0);

            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static String getVideoInfo(String input) throws UnsupportedEncodingException {

        JSONObject entryData = getVideoEntry("http://gdata.youtube.com/feeds/api/videos/" + input + "?v=2&alt=json");

        JSONObject entryTitle = (JSONObject) entryData.get("title");
        String videoTitle = urlDecode((String) entryTitle.get("$t"));

        JSONArray authorArray = (JSONArray) entryData.get("author");
        JSONObject authorObject = (JSONObject) authorArray.get(0);
        JSONObject authorName = (JSONObject) authorObject.get("name");
        String videoAuthor = urlDecode((String) authorName.get("$t"));

        JSONObject viewObject = (JSONObject) entryData.get("yt$statistics");
        String videoViews = (String) viewObject.get("viewCount");

        JSONObject mediaObject = (JSONObject) entryData.get("media$group");
        JSONObject durationObject = (JSONObject) mediaObject.get("yt$duration");
        String videoDuration = (String) durationObject.get("seconds");

        JSONArray categoryArray = (JSONArray) entryData.get("category");
        JSONObject categoryObject = (JSONObject) categoryArray.get(1);
        String videoCategory = (String) categoryObject.get("term");

        String videoLikes = "0";
        String videoDislikes = "0";

        if (entryData.containsKey("yt$rating")) {
            JSONObject ratingObject = (JSONObject) entryData.get("yt$rating");
            videoLikes = (String) ratingObject.get("numLikes");
            videoDislikes = (String) ratingObject.get("numDislikes");
        }

        return Colour.BOLD + videoTitle + Colour.RESET +
                " | " + videoAuthor +
                " | " + videoCategory +
                " | " + videoViews +
                " | " + Colour.GREEN + videoLikes + Colour.RESET +
                " / " + Colour.RED + videoDislikes + Colour.RESET +
                " | " + Utility.timeConversion(Integer.parseInt(videoDuration)) +
                " | " + "https://youtu.be/" + input;

    }

}
