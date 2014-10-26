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

public class UrbanDictionary {

    public static String[] getWord(String s) {

        try {
            s = s.replaceAll(" ", "+");

            URL url = new URL("http://api.urbandictionary.com/v0/define?term=" + s);

            URLConnection conn = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            JSONArray listObject = (JSONArray) jsonObject.get("list");

            JSONObject firstResult;

            if (listObject.isEmpty()) return new String[]{"No Results Found", "http://www.urbandictionary.com/"};
            else firstResult = (JSONObject) listObject.get(0);

            return new String[]{firstResult.get("word").toString(), firstResult.get("definition").toString()};

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return null;

    }

}
