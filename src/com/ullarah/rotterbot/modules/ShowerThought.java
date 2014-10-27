package com.ullarah.rotterbot.modules;

import com.ullarah.rotterbot.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static com.ullarah.rotterbot.Utility.urlDecode;

public class ShowerThought {

    public static String randomThought() {

        try {
            URL url = new URL("http://myshowerthought.com/php/data.php?id=" + Utility.randInt(0, 1050));
            URLConnection conn = url.openConnection();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String result = reader.readLine().replaceFirst("\\d+;;;", "");

            reader.close();

            return ("[THOUGHT] " + urlDecode(result));

        } catch (IOException ignored) {
        }

        return null;

    }

}
