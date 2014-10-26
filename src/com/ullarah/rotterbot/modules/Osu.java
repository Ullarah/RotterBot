package com.ullarah.rotterbot.modules;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static com.ullarah.rotterbot.Client.pluginKey;

public class Osu {

    private static JSONObject getAPI(String type, String args) {

        try {
            args = args.replaceAll(" ", "+");

            URL url = new URL("https://osu.ppy.sh/api/" + type + "?k=" + pluginKey("osu") + args);
            URLConnection conn = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JSONParser jsonParser = new JSONParser();

            JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);

            return (JSONObject) jsonArray.get(0);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;

    }

    private static String[] getBeatmap(String map) {

        JSONObject osuObject = getAPI("get_beatmaps", "&b=" + map);

        String title = (String) osuObject.get("title");
        String artist = (String) osuObject.get("artist");

        return new String[]{title, artist};

    }

    private static String getUser(String user) {

        JSONObject osuObject = getAPI("get_user", "&u=" + user);

        return (String) osuObject.get("username");

    }

    public static String getUserInfo(String user) {

        JSONObject osuObject = getAPI("get_user", "&u=" + user);

        String username = (String) osuObject.get("username");
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

    public static String getUserType(String user, String type) throws IOException, ParseException {

        JSONObject osuObject = getAPI(type, "&u=" + user + "&limit=1");

        String[] beatmap = getBeatmap((String) osuObject.get("beatmap_id"));

        String username = getUser(user);
        String title = beatmap[0];
        String artist = beatmap[1];
        String score = (String) osuObject.get("score");
        String maxcombo = (String) osuObject.get("maxcombo");
        String perfect = (String) osuObject.get("perfect");
        String rank = (String) osuObject.get("rank");

        return "[OSU] " + username + " | " + title + " by " + artist + " | SCORE: " + score +
                " | MAX: " + maxcombo + " | PER: " + perfect + " | RANK: " + rank;

    }

}
