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

public class Google {

    public static String[] feelingLucky(String s, Boolean isSafe) {

        String r;

        if (isSafe) r = "active";
        else r = "off";

        try {
            s = s.replaceAll(" ", "+");

            URL url = new URL("http://ajax.googleapis.com/ajax/services/search/web?v=1.0&safe=" + r + "&lr=lang_en&q=" + s);
            URLConnection conn = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            JSONObject responseData = (JSONObject) jsonObject.get("responseData");
            JSONArray resultData = (JSONArray) responseData.get("results");

            JSONObject firstResult;

            if (resultData.isEmpty()) return new String[]{"No Results Found", "http://www.google.com/"};
            else firstResult = (JSONObject) resultData.get(0);

            return new String[]{firstResult.get("titleNoFormatting").toString(), firstResult.get("url").toString()};

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;

    }

}
