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

import static com.ullarah.rotterbot.Utility.urlDecode;
import static com.ullarah.rotterbot.Utility.urlEncode;

public class Google {

    public static String feelingLucky(String input, Boolean safe) throws IOException, ParseException {

        String isSafe;

        if (safe) isSafe = "active";
        else isSafe = "off";

        URL url = new URL("http://ajax.googleapis.com/ajax/services/search/web?v=1.0&safe=" + isSafe + "&lr=lang_en&q=" + urlEncode(input));
        URLConnection conn = url.openConnection();

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

        reader.close();

        JSONObject responseData = (JSONObject) jsonObject.get("responseData");
        JSONArray resultData = (JSONArray) responseData.get("results");

        JSONObject firstResult;

        if (resultData.isEmpty()) return "[GOOGLE] No Results Found";

        firstResult = (JSONObject) resultData.get(0);

        return "[GOOGLE] " + urlDecode(firstResult.get("titleNoFormatting").toString()) + " | " + urlDecode(firstResult.get("url").toString());

    }

}
