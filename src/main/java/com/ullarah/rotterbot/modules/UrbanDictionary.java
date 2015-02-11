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

import static com.ullarah.rotterbot.Utility.urlEncode;

public class UrbanDictionary {

    public static String getWord(String input) throws IOException, ParseException {

        URL url = new URL("http://api.urbandictionary.com/v0/define?term=" + urlEncode(input));

        URLConnection conn = url.openConnection();

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

        reader.close();

        JSONArray listObject = (JSONArray) jsonObject.get("list");

        JSONObject firstResult;

        if (listObject.isEmpty()) return "[UD] No Results Found";

        firstResult = (JSONObject) listObject.get(0);

        return "[UD] " + Colour.BOLD + firstResult.get("word") + ": " + Colour.RESET + firstResult.get("definition");

    }

}
