package com.ullarah.rotterbot.modules;

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

import static com.ullarah.rotterbot.Client.pluginKey;
import static com.ullarah.rotterbot.Utility.urlDecode;
import static com.ullarah.rotterbot.Utility.urlEncode;

public class Osu {

    private static JSONObject getAPI(String type, String args) {

        try {

            URL url = new URL("https://osu.ppy.sh/api/" + type + "?k=" + pluginKey("osu") + args);
            URLConnection conn = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JSONParser jsonParser = new JSONParser();

            JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);

            reader.close();

            return (JSONObject) jsonArray.get(0);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;

    }

    private static String[] getBeatmap(String map) throws UnsupportedEncodingException {

        JSONObject osuObject = getAPI("get_beatmaps", "&b=" + urlEncode(map));

        String title = urlDecode((String) osuObject.get("title"));
        String artist = urlDecode((String) osuObject.get("artist"));

        return new String[]{title, artist};

    }

    private static String getUser(String user) throws UnsupportedEncodingException {

        JSONObject osuObject = getAPI("get_user", "&u=" + urlEncode(user));

        return urlDecode((String) osuObject.get("username"));

    }

    public static String getUserInfo(String user) throws UnsupportedEncodingException {

        JSONObject osuObject = getAPI("get_user", "&u=" + urlEncode(user));

        String username = urlDecode((String) osuObject.get("username"));
        String playcount = (String) osuObject.get("playcount");
        String rank = (String) osuObject.get("pp_rank");
        String pp = (String) osuObject.get("pp_raw");
        String accuracy = (String) osuObject.get("accuracy");
        String country = (String) osuObject.get("country");
        String count_ss = (String) osuObject.get("count_rank_ss");
        String count_s = (String) osuObject.get("count_rank_s");
        String count_a = (String) osuObject.get("count_rank_a");

        return "[OSU] " + username + " | " + country + " | " + pp + "pp" +
                " | RANK: " + rank + " | PLAYS: " + playcount + " | ACC: " + accuracy +
                " | SS: " + count_ss + " | S: " + count_s + " | A: " + count_a;

    }

    public static String getUserType(String user, String type) throws IOException {

        JSONObject osuObject = getAPI(urlEncode(type), "&u=" + urlEncode(user) + "&limit=1");

        String[] beatmap = getBeatmap((String) osuObject.get("beatmap_id"));

        String username = urlDecode(getUser(user));
        String title = urlDecode(beatmap[0]);
        String artist = urlDecode(beatmap[1]);
        String score = (String) osuObject.get("score");
        String maxcombo = (String) osuObject.get("maxcombo");
        String perfect = (String) osuObject.get("perfect");
        String rank = (String) osuObject.get("rank");

        return "[OSU] " + username + " | " + title + " by " + artist + " | SCORE: " + score +
                " | MAX: " + maxcombo + " | PER: " + perfect + " | RANK: " + rank;

    }

}
